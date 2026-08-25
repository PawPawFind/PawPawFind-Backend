# 매칭 후보 보호소 위치 응답

기능명세 5.3.2는 보호동물 매칭 후보에 보호소 텍스트 정보와 저장된 좌표를 함께 제공한다. 보호소 위치를 저장·지오코딩하는 5.3.1(Issue #38)을 선행 조건으로 사용하며, 매칭 요청 시 실시간 지오코딩은 수행하지 않는다.

## API

두 API는 동일한 `MatchQueryResponse.results[]` 후보 계약을 사용한다.

| Method | Path | 설명 |
|---|---|---|
| `POST` | `/api/reports/{reportId}/run-match` | 기존 AI 매칭을 한 번 실행·저장한 뒤 최신 결과 반환 |
| `GET` | `/api/reports/{reportId}/matches?limit=20` | 최신 DONE 실행의 저장된 후보 반환 |

기존 `rank`, 후보 식별자, 점수, 태그, 이미지 필드는 그대로 유지되고 후보 끝에 nullable `shelter`가 추가된다.

```json
{
  "matchRunId": 9,
  "reportId": 14,
  "modelVersion": "model-v1",
  "rerankVersion": "rerank-v1",
  "decision": "REVIEW",
  "status": "DONE",
  "results": [
    {
      "rank": 1,
      "candidateType": "SHELTER",
      "desertionNo": "A-1",
      "candidateReportId": null,
      "visualScore": 0.8,
      "rankingScore": 0.91,
      "tagScore": 0.7,
      "textScore": 0.6,
      "phashDistance": 3,
      "nearDuplicate": false,
      "matchedTags": { "color": "brown" },
      "conflictingTags": {},
      "galleryId": "animal:A-1",
      "imageUrl": "https://example.com/animal.jpg",
      "shelter": {
        "careRegNo": "REG-1",
        "name": "행복 보호소",
        "address": "서울시 송파구",
        "telephone": "02-1234-5678",
        "latitude": 37.5,
        "longitude": 127.1
      }
    },
    {
      "rank": 2,
      "candidateType": "REPORT",
      "desertionNo": null,
      "candidateReportId": 22,
      "shelter": null
    }
  ]
}
```

## 정보 책임과 null 정책

- `SHELTER` 후보에서 Animal이 존재하면 `careRegNo`, `name`, `address`, `telephone`은 Animal의 최신 공공데이터를 사용한다.
- 좌표는 동일한 보호소 식별 키로 조회한 ShelterLocation만 사용한다. 식별 키는 등록번호 우선, 주소 SHA-256 fallback이라는 Issue #38 규칙을 재사용한다.
- `geocodeStatus=SUCCESS`이고 위도·경도가 모두 존재하며 유한하고 정상 범위일 때만 두 좌표를 반환한다.
- 위치 레코드가 없거나 PENDING·NOT_FOUND·FAILED이거나 좌표 한쪽이 잘못된 경우 위도와 경도를 모두 `null`로 반환한다.
- 좌표가 없어도 보호소 텍스트 정보와 매칭 후보는 유지한다. Animal 자체가 없으면 `shelter=null`이다.
- `REPORT` 후보는 항상 `shelter=null`이다.

프론트엔드는 `candidateType=SHELTER`이고 `shelter.latitude`와 `shelter.longitude`가 모두 non-null일 때만 보호소 지도 마커를 표시한다. 좌표가 없으면 후보 상세의 보호소명·주소·전화번호는 계속 표시할 수 있다.

## 조회 및 외부 호출 정책

limit 적용 후 반환 대상 SHELTER 후보의 `desertionNo`를 중복 제거하여 Animal을 한 번에 조회하고, ShelterIdentity가 만든 `shelterKey`를 다시 중복 제거하여 ShelterLocation을 한 번에 조회한다. 같은 보호소를 공유하는 후보도 후보 순서에 맞게 안정적으로 조립된다.

매칭 결과 조회 과정에서는 카카오 Local API, 공공데이터 API 또는 AI API를 호출하지 않는다. 단, `run-match`는 기존 계약대로 AI `/match`를 한 번 호출한다. 위치가 없으면 다음 공공데이터 동기화 및 Issue #38의 지오코딩 흐름에서 점진적으로 채워진다.
