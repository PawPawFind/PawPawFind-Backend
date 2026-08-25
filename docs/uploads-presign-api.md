# S3 Presign 업로드 API

| 항목 | 내용 |
|------|------|
| 담당 | **강유진** |
| 목차 | [BACKEND.md](../BACKEND.md) · [제보 API](./reports-api.md) |

## 호출

`POST /api/uploads/presign`

인증 없이 제보 사진용 **S3 Presigned PUT URL**을 발급한다.  
FE는 `uploadUrl`로 파일을 PUT한 뒤, 받은 `photoUrl`을 `POST /api/report-photos`에 넣는다.

## 요청

```json
{
  "filename": "dog.jpg",
  "contentType": "image/jpeg"
}
```

| 필드 | 필수 | 설명 |
|------|---:|------|
| filename | 권장 | 확장자 추론에 사용. 없거나 애매하면 contentType 기준 확장자 |
| contentType | 예 | `image/jpeg` \| `image/png` \| `image/webp` (대소문자 무시, trim) |

허용되지 않은 contentType이면 HTTP 400, body에 메시지 문자열.

## 응답

```json
{
  "uploadUrl": "https://bucket.s3.region.amazonaws.com/reports/uuid.jpg?X-Amz-…",
  "photoUrl": "https://bucket.s3.region.amazonaws.com/reports/uuid.jpg",
  "objectKey": "reports/uuid.jpg"
}
```

| 필드 | 설명 |
|------|------|
| uploadUrl | Presigned PUT (유효 **10분**) |
| photoUrl | DB에 넣을 공개 URL |
| objectKey | 버킷 내 키 (`aws.s3.upload-prefix` + UUID + 확장자) |

## 설정

```properties
aws.region=ap-northeast-2
aws.s3.bucket=pawpawfind-report-photos
aws.s3.upload-prefix=reports/
```

자격 증명은 AWS SDK `DefaultCredentialsProvider`(로컬 프로파일 / EC2 역할 등).

## 사용 흐름

```text
1. POST /api/uploads/presign
2. HTTP PUT uploadUrl  (Content-Type = 요청한 contentType)
3. POST /api/report-photos { reportId, photoUrl, sortOrder }
```
