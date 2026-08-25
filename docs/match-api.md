# 유사 동물 매칭 API

| 항목 | 내용 |
|------|------|
| 담당 | **강유진** |
| 목차 | [README.md](../README.md) · [임베딩 Internal](./embedding-internal-api.md) |

제보 사진·특징을 AI `/match`에 보내 Top-N 후보를 받고 `match_runs` / `match_results`에 저장한다.  
SHELTER 후보에는 보호소 위치(`MatchShelterDto`)를 보강할 수 있다.

컨트롤러에 JWT 검사는 **없다**. (내부/운영에서 호출)

---

## `POST /api/reports/{reportId}/run-match`

BE가 제보의 `species`, 사진 URL 목록, 특징(`category`/`keyword`)을 모아 AI에 요청한 뒤 결과를 저장하고 반환한다.

**AI 요청 body** (`MatchAiRequest`)

```json
{
  "reportId": 12,
  "species": "강아지",
  "photoUrls": ["https://…/a.jpg"],
  "features": [
    { "category": "털색", "keyword": "갈색" }
  ]
}
```

AI 응답을 `MatchResultUpsertRequest`와 같은 형태로 해석해 저장한다.

| 상태 | 조건 |
|------|------|
| 200 | 저장·반환 성공 |
| 404 | 제보 없음 등 (응답 null) |
| 400 | `IllegalArgumentException` |
| 503 | `ai.service.url` 미설정·AI 호출 실패 등 `IllegalStateException` |

`species`는 AI 계약상 `강아지`/`고양이` 등 한글을 기대한다.

---

## `GET /api/reports/{reportId}/matches`

최신 매칭 실행의 Top-N.  
파라미터 `limit` 기본 **20**.

없으면 404.

---

## `POST /api/internal/match-results`

AI 또는 수동 테스트용 upsert. JWT 없음.

```json
{
  "reportId": 12,
  "modelVersion": "…",
  "rerankVersion": "…",
  "decision": "…",
  "results": [
    {
      "rank": 1,
      "candidateType": "SHELTER",
      "desertionNo": "441250202600001",
      "candidateReportId": null,
      "visualScore": 0.91,
      "rankingScore": 0.88,
      "tagScore": 0.7,
      "textScore": null,
      "phashDistance": 8,
      "nearDuplicate": false,
      "matchedTags": {},
      "conflictingTags": {},
      "galleryId": "…",
      "imageUrl": "https://…"
    }
  ]
}
```

`candidateType`:

- `SHELTER` → `desertionNo` 필요
- `REPORT` → `candidateReportId` 필요

존재하지 않는 보호소/제보 후보는 저장·조회 시 걸러질 수 있다(orphan 필터).

---

## 응답 예시 (`MatchQueryResponse`)

```json
{
  "matchRunId": 5,
  "reportId": 12,
  "modelVersion": "…",
  "rerankVersion": "…",
  "decision": "…",
  "status": "DONE",
  "createdAt": "2026-08-25T12:00:00",
  "results": [
    {
      "rank": 1,
      "candidateType": "SHELTER",
      "desertionNo": "441250202600001",
      "candidateReportId": null,
      "visualScore": 0.91,
      "rankingScore": 0.88,
      "tagScore": 0.7,
      "textScore": null,
      "phashDistance": 8,
      "nearDuplicate": false,
      "matchedTags": {},
      "conflictingTags": {},
      "galleryId": "shelter-…",
      "imageUrl": "https://…",
      "shelter": {
        "careRegNo": "…",
        "name": "○○유기동물보호소",
        "address": "서울…",
        "telephone": "02-…",
        "latitude": 37.5,
        "longitude": 127.0
      }
    }
  ]
}
```

`shelter`는 SHELTER 후보에 대해 `animals` / `shelter_locations`에서 조립한다. 좌표가 없으면 latitude/longitude는 null일 수 있다.

## 설정

```properties
ai.service.url=http://127.0.0.1:8000
```
