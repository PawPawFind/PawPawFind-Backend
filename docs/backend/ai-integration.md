# BE ↔ AI 연동

기준일: 2026-08-25.

Backend는 AI 서비스(`ai.service.url`, 기본 `http://127.0.0.1:8000`)를 호출하고, AI는 Backend의 `/api/internal/**`로 갤러리·임베딩·매칭 결과를 주고받는다.

## 호출 방향

| 방향 | 시점 | 대상 |
|------|------|------|
| BE → AI | `POST /api/reports/{id}/run-match` | `POST {ai}/match` |
| BE → AI | `POST /api/reports/{id}/search-areas` | `POST {ai}/search-areas` |
| BE → AI | 제보 사진 생성 후 (비동기) | `POST {ai}/embed/report-photo` |
| BE → AI | 보호소 sync 후 임베딩 없을 때 (비동기) | `POST {ai}/embed/animal` |
| AI → BE | 매칭/임베딩 파이프라인 | `/api/internal/*` |

## Match

1. FE/운영자가 `POST /api/reports/{reportId}/run-match`
2. BE가 제보 사진 URL·특징을 모아 AI `/match` 호출
3. 응답을 `match_runs` / `match_results`에 저장
4. FE는 `GET /api/reports/{reportId}/matches`로 조회

`species`는 AI 계약상 `강아지` / `고양이` 등 **한글**을 기대한다. DB에 `DOG`만 있으면 AI 422가 날 수 있다.

고아(orphan) 보호소 임베딩·삭제된 제보 후보는 저장/조회 시 걸러진다.

## Search areas

- BE wrapper: `POST /api/reports/{reportId}/search-areas` (작성자/ADMIN)
- 제보 위치·크기·시간·행동 태그를 AI에 전달, **응답만 반환(미저장)**
- V1: `LOST` + `강아지`
- Overpass 실패 시 AI가 `fallbackUsed=true`로도 200 가능 → BE는 정상 응답으로 취급

타임아웃·행동 태그 매핑: [search-area-api.md](../search-area-api.md)

## Embed 트리거

| 트리거 | 서비스 | AI |
|--------|--------|-----|
| `POST /api/report-photos` 성공 | `ReportEmbedTriggerService` | `/embed/report-photo` |
| 보호소 공고 sync | `AnimalEmbedTriggerService` | `/embed/animal` |

AI가 벡터를 만든 뒤 `POST /api/internal/*-embeddings/batch`로 BE에 적재한다.

## Internal API (요약)

| Path | 용도 |
|------|------|
| `POST /api/internal/animal-embeddings/batch` | 보호소 갤러리 upsert |
| `POST /api/internal/report-embeddings/batch` | 제보 사진 임베딩 upsert |
| `GET /api/internal/gallery/for-search` | 매칭용 갤러리 (`species`, `modelVersion`, …) |
| `GET /api/internal/animals/for-embedding` | 임베딩 미완료 공고 |
| `GET /api/internal/report-photos/for-embedding` | 임베딩 미완료 제보 사진 |
| `POST /api/internal/match-results` | 매칭 결과 upsert |

**보안:** 코드에 internal API 키가 없다. 운영에서는 AI EC2 ↔ BE EC2 private 통신으로만 노출한다.

## 설정

```properties
ai.service.url=http://127.0.0.1:8000
ai.service.connect-timeout-millis=2000
ai.service.read-timeout-millis=15000
```

`ai.service.url`은 비어 있으면 기동 실패(search-areas 설정 검증).  
search-areas 전용 RestClient에 connect/read timeout이 적용된다. match/embed 클라이언트는 별도 RestClient를 쓴다.

→ [configuration.md](./configuration.md)
