# 임베딩 · 갤러리 Internal API

| 항목 | 내용 |
|------|------|
| 담당 | **강유진** |
| 목차 | [README.md](../README.md) · [매칭 API](./match-api.md) |

AI batch job과 `/match`용 갤러리 export를 위한 **internal** API다.  
컨트롤러에 JWT/API Key 검사가 **없다**. 같은 VPC·방화벽으로만 노출하는 전제다.

---

## `POST /api/internal/animal-embeddings/batch`

보호소 사진 임베딩 upsert.

**응답**

```json
{ "upserted": 10, "skipped": 2 }
```

잘못된 body면 400(empty body).

---

## `POST /api/internal/report-embeddings/batch`

제보 사진 임베딩 upsert. 응답 형식 동일(`EmbeddingBatchResponse`).

---

## `GET /api/internal/gallery/for-search`

매칭 검색용 갤러리 export.

| 파라미터 | 필수 | 기본 | 설명 |
|---|---:|---|---|
| species | 예 | - | 종 필터 |
| modelVersion | 예 | - | 모델 버전 |
| preprocessVersion | 예 | - | 전처리 버전 |
| excludeReportId | 아니요 | - | 제외할 제보 |
| includeAnimals | 아니요 | true | 보호소 갤러리 포함 |
| includeReports | 아니요 | true | 제보 갤러리 포함 |

**응답 요약** (`GallerySearchResponse`)

```json
{
  "modelVersion": "…",
  "preprocessVersion": "…",
  "animals": [
    {
      "galleryId": "…",
      "desertionNo": "…",
      "species": "강아지",
      "imageUrl": "https://…",
      "phashFull": "…",
      "phashCrop": "…",
      "embeddingFull": [0.1, 0.2],
      "embeddingCrop": [0.1, 0.2],
      "detectionConfidence": 0.95,
      "blurScore": 12.0,
      "metadata": {}
    }
  ],
  "reports": [ ]
}
```

animals에 `animals` 행이 없는 orphan 임베딩은 제외된다.

---

## `GET /api/internal/animals/for-embedding`

임베딩 대상 보호소 공고 export.

| 파라미터 | 필수 | 기본 | 설명 |
|---|---:|---|---|
| since | 아니요 | - | `LocalDateTime` 파싱 문자열. 이후 변경분 |
| missingOnly | 아니요 | false | true면 임베딩 없는 것만 |

`since` 형식이 잘못되면 400.

---

## `GET /api/internal/report-photos/for-embedding`

| 파라미터 | 필수 | 기본 | 설명 |
|---|---:|---|---|
| missingOnly | 아니요 | true | 임베딩 없는 사진만 |

---

## BE → AI 트리거 (참고)

HTTP internal이 아니라 서비스가 비동기로 호출한다.

| 시점 | AI 경로 |
|------|---------|
| `POST /api/report-photos` 성공 | `POST {ai.service.url}/embed/report-photo` |
| 보호소 sync 후 미임베딩 | `POST {ai.service.url}/embed/animal` |

```properties
ai.service.url=http://127.0.0.1:8000
```
