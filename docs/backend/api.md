# Backend API 목록

기준일: 2026-08-25. 상세 스펙은 Swagger(`/swagger-ui.html`)와 아래 링크를 우선한다.

인증 표기:

- **Public** — JWT 없이 호출 가능
- **Optional JWT** — 있으면 `userId` 부여
- **JWT** — `Authorization: Bearer <token>` 필수
- **Owner/ADMIN** — JWT + 제보 작성자 또는 `ADMIN`
- **Internal** — `/api/internal/**`, 앱 레벨 인증 없음

---

## Health

| Method | Path | Auth | 설명 |
|--------|------|------|------|
| `GET` | `/api/health` | Public | 백엔드 생존 확인 |

응답 예: `{ "status": "UP", "service": "..." }`

---

## Auth

| Method | Path | Auth | 설명 |
|--------|------|------|------|
| `POST` | `/api/auth/kakao` | Public | 카카오 `code` → JWT |

요청: `{ "code": "<kakao authorization code>" }`  
응답: `accessToken`, `userId`, `nickname`, `provider`, `role`  
→ [auth.md](./auth.md)

---

## Uploads (S3)

| Method | Path | Auth | 설명 |
|--------|------|------|------|
| `POST` | `/api/uploads/presign` | Public | 제보 사진용 S3 PUT URL |

요청: `filename`, `contentType` (`image/jpeg` \| `image/png` \| `image/webp`)  
응답: `uploadUrl`, `photoUrl`, `objectKey`

---

## Animals (보호소 공고)

| Method | Path | Auth | 설명 |
|--------|------|------|------|
| `GET` | `/api/animals` | Public | DB에 저장된 공고 목록 |
| `GET` | `/api/animals/{desertionNo}` | Public | 공고 상세 (없으면 404) |
| `GET` | `/abandoned` | Public | 공공 API 동기화 후 전체 목록 반환 |

스케줄: 매 **1시간** 자동 sync (`AbandonedService`). `/abandoned`는 수동 트리거.

---

## Reports

| Method | Path | Auth | 설명 |
|--------|------|------|------|
| `POST` | `/api/reports` | Optional JWT | 제보 생성 (JWT 있으면 `userId` 저장) |
| `GET` | `/api/reports` | Public | 페이지 목록 (`page`, `size`, optional `reportType`) |
| `GET` | `/api/reports/me` | JWT | 내 제보 |
| `GET` | `/api/reports/{reportId}` | Public | 상세 |
| `PUT` | `/api/reports/{reportId}` | Owner/ADMIN | 수정 |
| `DELETE` | `/api/reports/{reportId}` | Owner/ADMIN | 삭제 (사진·특징 포함) |

### 제보 본문 주요 필드 (`Reports`)

| 필드 | 설명 |
|------|------|
| `reportType` | `LOST` \| `FOUND` |
| `title` | FOUND용 제목 (선택) |
| `species` | 예: `강아지`, `고양이` (AI 연동 시 한글종 권장) |
| `size` | `소형` \| `중형` \| `대형` |
| `eventDate` | 실종/목격일 |
| `eventHour` | 0–23, 모르면 null |
| `happenPlace` | 장소 문자열 |
| `latitude`, `longitude` | 좌표 |
| `description` | 설명 |
| `status` | 기본 `OPEN` |

목록 응답(`ReportListItemResponse`)에 `thumbnailUrl`(가장 작은 `sortOrder` 사진) 포함.

### Photos

| Method | Path | Auth | 설명 |
|--------|------|------|------|
| `POST` | `/api/report-photos` | Owner/ADMIN (`reportId` 기준) | URL 등록 (제보당 최대 3장) → 비동기 embed |
| `GET` | `/api/report-photos?reportId=` | Public | 제보별 사진 |
| `GET` | `/api/report-photos/{id}` | Public | 단건 |
| `PUT` / `DELETE` | `/api/report-photos/{id}` | Owner/ADMIN | 수정 / 삭제 |

본문: `reportId`, `photoUrl`, `sortOrder`

### Features (태그)

| Method | Path | Auth | 설명 |
|--------|------|------|------|
| `POST` | `/api/report-features` | Owner/ADMIN | 태그 추가 (`털색` 최대 3) |
| `GET` | `/api/report-features?reportId=` | Public | 제보별 특징 |
| `GET` | `/api/report-features/{id}` | Public | 단건 |
| `PUT` / `DELETE` | `/api/report-features/{id}` | Owner/ADMIN | 수정 / 삭제 |

본문: `reportId`, `category`, `keyword`  
수색 영역용 행동 카테고리 매핑 → [search-area-api.md](../search-area-api.md)

---

## Nearby

담당: **김가윤** (지도 · 주변 제보 UI/API)

| Method | Path | Auth | 설명 |
|--------|------|------|------|
| `GET` | `/api/reports/nearby` | Public | 좌표 주변 OPEN 제보 |

파라미터: `latitude`, `longitude` (필수), `radiusMeters`(기본 3000, 최대 20000), `reportType`, `species`, `page`, `size`  
→ [nearby-reports-api.md](../nearby-reports-api.md) · [contributors.md](./contributors.md)

---

## Search areas

| Method | Path | Auth | 설명 |
|--------|------|------|------|
| `POST` | `/api/reports/{reportId}/search-areas` | Owner/ADMIN | AI 추천 수색 영역 (body 없음, DB 미저장) |

V1: `LOST` + `강아지`만.  
→ [search-area-api.md](../search-area-api.md)

---

## Match

| Method | Path | Auth | 설명 |
|--------|------|------|------|
| `POST` | `/api/reports/{reportId}/run-match` | Public | BE → AI `/match` 후 결과 저장 |
| `GET` | `/api/reports/{reportId}/matches` | Public | 최신 매칭 Top-N (`limit` 기본 20) |
| `POST` | `/api/internal/match-results` | Internal | AI가 결과 upsert |

응답(`MatchQueryResponse`): `matchRunId`, `reportId`, `modelVersion`, `rerankVersion`, `decision`, `status`, `createdAt`, `results[]`  
후보: `SHELTER`(`desertionNo`) 또는 `REPORT`(`candidateReportId`) + 점수·태그·이미지 URL

---

## Internal (AI ↔ BE)

앱 단 API 키 없음. 같은 VPC에서만 열 것.

| Method | Path | 설명 |
|--------|------|------|
| `POST` | `/api/internal/animal-embeddings/batch` | 보호소 임베딩 upsert |
| `POST` | `/api/internal/report-embeddings/batch` | 제보 사진 임베딩 upsert |
| `GET` | `/api/internal/gallery/for-search` | 매칭용 갤러리 export |
| `GET` | `/api/internal/animals/for-embedding` | 임베딩 대상 공고 |
| `GET` | `/api/internal/report-photos/for-embedding` | 임베딩 대상 제보 사진 |
| `POST` | `/api/internal/match-results` | 매칭 결과 저장 |

→ [ai-integration.md](./ai-integration.md)

---

## 공통 오류 (요약)

| HTTP | 의미 |
|------|------|
| 400 | 파라미터 검증 실패 |
| 401 | JWT 없음/무효 (보호 API) |
| 403 | 작성자/ADMIN 아님 |
| 404 | 리소스 없음 |
| 422 | 비즈니스 규칙 위반 (예: search-areas 종/유형) |
| 502 / 503 | AI 응답 이상 / 연결 실패·timeout |
