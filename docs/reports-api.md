# 제보 · 사진 · 특징 API

| 항목 | 내용 |
|------|------|
| 담당 | **강유진** |
| 목차 | [README.md](../README.md) |

실종(`LOST`) / 목격(`FOUND`) 제보와 사진 URL·특징 태그 API다.
**생성(POST)** 은 비로그인 가능하고, **수정·삭제** 는 작성자 또는 `ADMIN`만 가능하다.

---

## 제보

### `POST /api/reports`

JWT가 있으면 `userId`를 저장하고, 없어도 생성된다.

**요청 본문** (`Reports` 필드)

| 필드 | 필수 | 설명 |
|------|---:|------|
| reportType | 예 | `LOST` 또는 `FOUND` |
| title | 아니요 | FOUND용 제목 |
| species | 예 | 예: `강아지`, `고양이` (AI 연동 시 한글 권장) |
| size | 예 | 예: `소형`, `중형`, `대형` |
| eventDate | 예 | 실종/목격일 |
| eventHour | 아니요 | 0–23, 모르면 null |
| happenPlace | 예 | 장소 문자열 |
| latitude, longitude | 예 | 좌표 |
| description | 아니요 | 설명 |
| status | 아니요 | 기본 `OPEN` |

**응답:** 저장된 `Reports` JSON (`reportId`, `createdAt` 등 포함).

### `GET /api/reports`

| 파라미터 | 필수 | 기본 | 설명 |
|---|---:|---|---|
| page | 아니요 | 0 | 페이지 |
| size | 아니요 | 20 | 크기 |
| reportType | 아니요 | - | `LOST` / `FOUND` |

최신순(`createdAt` desc). 응답은 Spring `Page<ReportListItemResponse>`.

### `GET /api/reports/me`

JWT **필수**. 없으면 401 `"로그인이 필요합니다."`  
파라미터: `page`, `size` (기본 0 / 20).

### `GET /api/reports/{reportId}`

공개. 없으면 404.

### `PUT /api/reports/{reportId}` / `DELETE /api/reports/{reportId}`

작성자 또는 ADMIN. 비로그인 401, 권한 없음 403, 없음 404.  
삭제는 임베딩·사진·특징을 함께 지운다(`@Transactional`).

### 목록 응답 예시 (`ReportListItemResponse`)

```json
{
  "content": [
    {
      "reportId": 12,
      "userId": 3,
      "reportType": "FOUND",
      "title": "갈색 강아지 목격",
      "species": "강아지",
      "size": "소형",
      "eventDate": "2026-08-24",
      "eventHour": 15,
      "happenPlace": "서울시 중구",
      "latitude": 37.5665,
      "longitude": 126.978,
      "description": "설명",
      "status": "OPEN",
      "createdAt": "2026-08-24T12:00:00",
      "updatedAt": "2026-08-24T12:00:00",
      "thumbnailUrl": "https://bucket.s3.region.amazonaws.com/reports/...."
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

`thumbnailUrl`은 해당 제보 사진 중 `sortOrder`가 가장 작은 항목(동률이면 id 작은 쪽)의 URL이다. 없으면 `null`.

---

## 사진 (`report_photos`)

파일 바이너리는 받지 않고 **URL만** 저장한다. 업로드는 [uploads-presign-api.md](./uploads-presign-api.md) 후 S3 PUT.

### `POST /api/report-photos`

인증 없음. 제보당 최대 **3장**. 성공 시 비동기로 AI `POST {ai.service.url}/embed/report-photo` 트리거.

```json
{
  "reportId": 12,
  "photoUrl": "https://….amazonaws.com/reports/….jpg",
  "sortOrder": 0
}
```

| 오류 | 조건 |
|------|------|
| 400 | 본문/reportId 없음, 3장 초과 (body에 메시지 문자열) |
| 404 | 제보 없음 |

### `GET /api/report-photos?reportId=`

공개. 해당 제보 사진 리스트.

### `GET /api/report-photos/{reportPhotoId}`

공개. 없으면 404.

### `PUT` / `DELETE /api/report-photos/{reportPhotoId}`

작성자 또는 ADMIN. PUT 성공 시 embed 재트리거. DELETE 시 관련 `report_embeddings`도 삭제.

---

## 특징 (`report_features`)

### `POST /api/report-features`

인증 없음.

```json
{
  "reportId": 12,
  "category": "털색",
  "keyword": "갈색"
}
```

`category`가 `털색`이면 제보당 최대 **3개**. 초과 시 400.

### `GET /api/report-features?reportId=` / `GET …/{id}`

공개.

### `PUT` / `DELETE …/{reportFeatureId}`

작성자 또는 ADMIN.

행동 특징(활동량 등) category·keyword 규약은 수색 영역 API 문서를 본다: [search-area-api.md](./search-area-api.md).

---

## 권한 요약

| 동작 | 규칙 |
|------|------|
| POST reports / photos / features | 비로그인 가능 |
| GET (목록·상세·me 제외) | 공개 |
| GET /api/reports/me | JWT 필수 |
| PUT / DELETE | JWT + 작성자 또는 ADMIN |
| userId가 null인 익명 제보 | 일반 USER는 수정/삭제 불가, ADMIN만 가능 |
