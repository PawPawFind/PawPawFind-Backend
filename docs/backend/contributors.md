# 개발 담당

기준일: 2026-08-25.

| 이름 | 역할 |
|------|------|
| **강유진** | Backend 전반 (아래 김가윤 담당 제외) |
| **김가윤** | 지도 · 주변 제보 · 추천 수색 영역 BE 연동 |

## 강유진

- 카카오 로그인 · JWT · USER/ADMIN
- 제보 CRUD · 사진 · 특징 태그 · S3 presign
- 보호소 공고 수집·동기화 · `/api/animals`
- AI 매칭 연동 (`run-match` · matches · internal match/embed)
- 배포·CI · 운영 설정 · Swagger · **이 문서군**

## 김가윤

- 지도(카카오맵) UI·연동
- 지도 기준 **주변 제보** 조회·표시  
  → API: [`GET /api/reports/nearby`](../nearby-reports-api.md)
- **추천 수색 영역** BE 연동  
  → API: [`POST /api/reports/{reportId}/search-areas`](../search-area-api.md)
