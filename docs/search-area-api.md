# 추천 수색 영역 백엔드 연동 API

| 항목 | 내용 |
|------|------|
| 담당 | **김가윤** (추천 수색 영역 BE 연동) |
| 목차 | [README.md](../README.md) |

## 호출

`POST /api/reports/{reportId}/search-areas`

요청 body 없이 `reportId`에 해당하는 신고의 위치·시간·크기·설명과 행동 특징을 사용한다.
신고 작성자 또는 `ADMIN`만 호출할 수 있으며, V1은 `LOST` 유형의 강아지 신고만 지원한다.
AI 추천 결과는 요청할 때 계산하여 그대로 반환하며 DB에는 저장하지 않는다.

응답의 `center.latitude`, `center.longitude`, `radiusMeters`는 카카오맵 원 오버레이에 사용할 수 있다.
`priorityScore`는 발견 확률이 아니라 각 영역의 **수색 우선점수**다. AI가 Overpass 조회에 실패해
`fallbackUsed=true`를 반환해도 정상 응답으로 취급한다.

## 행동 특징 매핑

`report_features.category`별로 `id`가 가장 큰 최신 행을 선택한다. category와 keyword의 앞뒤
공백을 제거하고 keyword를 대문자로 정규화한다. 모르는 category와 털색·외형 특징은 무시한다.
누락되거나 허용되지 않은 keyword는 `UNKNOWN`으로 전달한다.

| category | AI 필드 | 허용 keyword |
|---|---|---|
| 활동량 | activityLevel | LOW, MEDIUM, HIGH, UNKNOWN |
| 낯선사람반응 | strangerResponse | APPROACH, NEUTRAL, AVOID, UNKNOWN |
| 소음민감도 | noiseSensitivity | LOW, MEDIUM, HIGH, UNKNOWN |
| 추격성향 | chaseTendency | LOW, MEDIUM, HIGH, UNKNOWN |
| 이동성 | mobility | NORMAL, LIMITED, UNKNOWN |
| 도주원인 | escapeCause | DOOR_OPEN, NOISE, CHASE, UNKNOWN |

행동 특징이 전혀 없어도 여섯 필드를 모두 `UNKNOWN`으로 채워 AI에 요청한다.

## 오류 응답

| 상태 | 조건 |
|---|---|
| 401 | 인증 정보 없음 |
| 403 | 신고 작성자 또는 관리자가 아님 |
| 404 | 신고 없음 |
| 422 | LOST 또는 강아지 신고가 아님, AI가 요청을 422로 거부 |
| 503 | AI 연결 실패 또는 timeout |
| 502 | AI의 그 외 오류, 빈 응답 또는 응답 해석 실패 |

## 설정

```properties
ai.service.url=http://127.0.0.1:8000
ai.service.connect-timeout-millis=2000
ai.service.read-timeout-millis=15000
```

`ai.service.url`은 비어 있을 수 없으며 timeout은 밀리초 단위의 양수여야 한다. AI의 Overpass
기본 timeout이 10초이므로 read timeout은 이보다 긴 15초를 기본값으로 사용한다. 설정은
애플리케이션 시작 시 검증되고 전용 HTTP 클라이언트에 한 번 적용된다.

## V1/V2 비교 API

기존 `POST /api/reports/{reportId}/search-areas`의 응답과 동작은 유지한다.
비교 기능은 별도의 다음 API를 호출한다.

```http
POST /api/reports/1/search-areas/compare
Authorization: Bearer <JWT>
Accept: application/json
```

요청 body와 query parameter는 없다. `reportId`는 Long이다. 기존 JWT 필터의 userId/role을
사용하며 신고 작성자 또는 ADMIN만 접근 가능하다. LOST 유형의 강아지 신고만 지원한다.
권한 검사와 신고 검증이 끝나기 전에 AI를 호출하지 않는다.

기존 추천과 동일한 SearchAreaRequestMapper로 신고의 reportId, species, size, eventDate,
eventHour, latitude, longitude, happenPlace, description 및 behaviorProfile을 준비한다.
SMALL/MEDIUM/LARGE는 소형/중형/대형으로 정규화한다. 행동 특징은 위 매핑 표와 동일하며
category별 최신 id를 사용하고 누락·유효하지 않은 keyword는 UNKNOWN이다.

Backend는 기존 searchAreaRestClient와 AI Base URL을 사용해
`POST {ai.service.url}/search-areas/compare`를 **정확히 한 번** 호출한다.
Content-Type과 Accept는 application/json이다. AI가 동일 신고·단일 기준 시각·단일 환경 조회로
V1/V2를 계산한다. Backend는 점수, 중심 이동 거리, 점수 차이를 재계산하거나 DB에 저장하지 않는다.
환경변수, Secret, DB 설정을 추가하지 않는다.

계약 기준:
[AI 비교 API 문서](https://github.com/PawPawFind/PawPawFind-AI/blob/develop/docs/search-area-v2.md),
[요청 예시](https://github.com/PawPawFind/PawPawFind-AI/blob/develop/docs/examples/search-area-compare-request.json),
[응답 예시](https://github.com/PawPawFind/PawPawFind-AI/blob/develop/docs/examples/search-area-compare-response.json).

### 전체 응답 예시

아래는 AI TestClient의 고정 입력·고정 시각과 북쪽 75m 녹지 대표점 하나를 사용한
FIXED_TEST_DATA 예시이며 실서비스 Overpass 결과가 아니다.

```json
{
  "reportId": 1,
  "comparedAt": "2026-10-29T12:00:00+09:00",
  "baseline": {
    "algorithmVersion": "HEURISTIC_V1",
    "behaviorType": "FEARFUL",
    "estimatedRadiusMeters": 1000,
    "environmentSource": "FIXED_TEST_DATA",
    "fallbackUsed": false,
    "assumptions": [],
    "areas": [
      {
        "rank": 1,
        "center": {
          "latitude": 37.5665,
          "longitude": 126.978
        },
        "radiusMeters": 150,
        "priorityScore": 92,
        "reasonCodes": [
          "FEARFUL_HIDE",
          "GREEN_SPACE",
          "LOW_TRAFFIC"
        ],
        "reason": "두려움이 강한 행동 특성 및 주변 녹지와 이동 가능 경로을 고려한 우선 수색 영역입니다.",
        "scoreBreakdown": {
          "distance": 30,
          "behaviorEnvironment": 30,
          "corridor": 14,
          "accessibility": 10,
          "eventContext": 8
        }
      },
      {
        "rank": 2,
        "center": {
          "latitude": 37.564701,
          "longitude": 126.978
        },
        "radiusMeters": 150,
        "priorityScore": 86,
        "reasonCodes": [
          "FEARFUL_HIDE",
          "GREEN_SPACE",
          "LOW_TRAFFIC"
        ],
        "reason": "두려움이 강한 행동 특성 및 주변 녹지와 이동 가능 경로을 고려한 우선 수색 영역입니다.",
        "scoreBreakdown": {
          "distance": 24,
          "behaviorEnvironment": 30,
          "corridor": 14,
          "accessibility": 10,
          "eventContext": 8
        }
      }
    ]
  },
  "improved": {
    "algorithmVersion": "HEURISTIC_V2",
    "behaviorType": "FEARFUL",
    "estimatedRadiusMeters": 1000,
    "environmentSource": "FIXED_TEST_DATA",
    "fallbackUsed": false,
    "assumptions": [],
    "areas": [
      {
        "rank": 1,
        "center": {
          "latitude": 37.5665,
          "longitude": 126.978
        },
        "radiusMeters": 150,
        "priorityScore": 81,
        "reasonCodes": [
          "DISTANCE_FROM_LAST_SEEN",
          "BEHAVIOR_NEAR_GREEN_SPACE",
          "CORRIDOR_NEAR_GREEN_SPACE"
        ],
        "reason": "실종 위치와의 거리가 우선점수에 반영되었습니다. 두려움 관련 행동 특성을 고려한 행동·환경 적합도에 구성 격자와 녹지 대표점의 가까운 거리가 반영되었습니다. 두려움 관련 행동 특성을 고려한 이동 통로 관련 점수에 구성 격자와 녹지 대표점의 가까운 거리가 반영되었습니다. 우선 수색을 위한 참고 정보입니다.",
        "scoreBreakdown": {
          "distance": 30,
          "behaviorEnvironment": 23,
          "corridor": 10,
          "accessibility": 10,
          "eventContext": 8
        }
      },
      {
        "rank": 2,
        "center": {
          "latitude": 37.568299,
          "longitude": 126.978
        },
        "radiusMeters": 150,
        "priorityScore": 68,
        "reasonCodes": [
          "DISTANCE_FROM_LAST_SEEN",
          "BEHAVIOR_NEAR_GREEN_SPACE",
          "CORRIDOR_NEAR_GREEN_SPACE"
        ],
        "reason": "실종 위치와의 거리가 우선점수에 반영되었습니다. 두려움 관련 행동 특성을 고려한 행동·환경 적합도에 구성 격자와 녹지 대표점의 가까운 거리가 반영되었습니다. 두려움 관련 행동 특성을 고려한 이동 통로 관련 점수에 구성 격자와 녹지 대표점의 가까운 거리가 반영되었습니다. 우선 수색을 위한 참고 정보입니다.",
        "scoreBreakdown": {
          "distance": 24,
          "behaviorEnvironment": 18,
          "corridor": 8,
          "accessibility": 10,
          "eventContext": 8
        }
      }
    ]
  },
  "comparison": {
    "fallbackUsed": false,
    "summary": "추천 영역 2개 → 2개. 동일 순위 중심 이동 최대 400.1m, 점수 변화 -18~-11점. 동일 순위 비교는 동일 물리 영역의 추적이 아니며 점수 차이는 정확도 차이를 뜻하지 않습니다.",
    "areaCountBefore": 2,
    "areaCountAfter": 2,
    "rankChanges": [
      {
        "rank": 1,
        "status": "BOTH",
        "beforeCenter": {
          "latitude": 37.5665,
          "longitude": 126.978
        },
        "afterCenter": {
          "latitude": 37.5665,
          "longitude": 126.978
        },
        "centerShiftMeters": 0.0,
        "beforeRadiusMeters": 150,
        "afterRadiusMeters": 150,
        "beforePriorityScore": 92,
        "afterPriorityScore": 81,
        "scoreDelta": -11,
        "addedReasonCodes": [
          "DISTANCE_FROM_LAST_SEEN",
          "BEHAVIOR_NEAR_GREEN_SPACE",
          "CORRIDOR_NEAR_GREEN_SPACE"
        ],
        "removedReasonCodes": [
          "FEARFUL_HIDE",
          "GREEN_SPACE",
          "LOW_TRAFFIC"
        ]
      },
      {
        "rank": 2,
        "status": "BOTH",
        "beforeCenter": {
          "latitude": 37.564701,
          "longitude": 126.978
        },
        "afterCenter": {
          "latitude": 37.568299,
          "longitude": 126.978
        },
        "centerShiftMeters": 400.08,
        "beforeRadiusMeters": 150,
        "afterRadiusMeters": 150,
        "beforePriorityScore": 86,
        "afterPriorityScore": 68,
        "scoreDelta": -18,
        "addedReasonCodes": [
          "DISTANCE_FROM_LAST_SEEN",
          "BEHAVIOR_NEAR_GREEN_SPACE",
          "CORRIDOR_NEAR_GREEN_SPACE"
        ],
        "removedReasonCodes": [
          "FEARFUL_HIDE",
          "GREEN_SPACE",
          "LOW_TRAFFIC"
        ]
      }
    ]
  }
}
```

### 응답 필드 및 nullable 계약

모든 JSON 필드는 camelCase다. 최상위 reportId만 존재하며 baseline/improved 내부에는 없다.

| 필드 | 타입 / 의미 |
|---|---|
| reportId | Long, 요청 신고 ID |
| comparedAt | offset을 포함한 ISO-8601 문자열. Backend OffsetDateTime으로 +09:00 등을 보존 |
| baseline | SearchAreaAlgorithmResponse, HEURISTIC_V1 |
| improved | SearchAreaAlgorithmResponse, HEURISTIC_V2 |
| comparison | SearchAreaComparisonResponse |

양쪽 알고리즘 결과는 algorithmVersion(String), behaviorType(String), estimatedRadiusMeters(Integer),
environmentSource(String), fallbackUsed(Boolean), assumptions(String 배열), areas(영역 배열)를 포함한다.
각 영역은 rank(Integer), center({latitude: Double, longitude: Double}), radiusMeters(Integer),
priorityScore(Integer), scoreBreakdown(객체 또는 null), reasonCodes(String 배열), reason(String)이다.
비교 DTO의 반경·점수는 정수이고 기존 V1 API의 Double 필드 및 JSON 구조는 변경하지 않는다.
기존 V1 API에는 scoreBreakdown 필드가 추가되지 않는다.

scoreBreakdown은 정규화 원점수가 아닌 최종 priorityScore에 기여한 **정수 점수**다.
합계는 priorityScore와 같으며 fallback일 때는 `"scoreBreakdown": null`을 명시적으로 반환한다.

| 필드 | 최대 점수 | 의미 |
|---|---:|---|
| distance | 30 | 실종 위치로부터 거리 |
| behaviorEnvironment | 30 | 행동 특성과 주변 환경 적합도 |
| corridor | 20 | 이동 통로 관련 환경 |
| accessibility | 10 | 도로·장벽 대표점과의 거리를 반영한 접근성 |
| eventContext | 10 | 행동 및 실종 상황 |

comparison은 fallbackUsed(Boolean), summary(String), areaCountBefore/areaCountAfter(Integer),
rankChanges(배열)를 포함한다. fallbackUsed는 어느 쪽이든 fallback이면 true다.

| rankChanges 필드 | 타입 / nullable 조건 |
|---|---|
| rank | Integer, 비교 순위 |
| status | String, BOTH / ADDED / REMOVED |
| beforeCenter / afterCenter | center 객체 또는 null; 해당 쪽 순위가 없으면 null |
| centerShiftMeters | Double 또는 null; 양쪽 존재 시 동일 순위 중심점 거리(m), 그 외 null |
| beforeRadiusMeters / afterRadiusMeters | Integer 또는 null; 해당 쪽 순위가 없으면 null |
| beforePriorityScore / afterPriorityScore | Integer 또는 null; 해당 쪽 순위가 없으면 null |
| scoreDelta | Integer 또는 null; 양쪽 존재 시 after - before, 그 외 null |
| addedReasonCodes / removedReasonCodes | String 배열; 추가/제거된 이유 코드, AI 순서 유지 |

BOTH는 같은 rank에 양쪽 결과가 있다는 의미다. ADDED는 after만, REMOVED는 before만 존재한다.
nullable 필드는 누락하지 않고 명시적 null로 전달한다. null인 이동 거리나 점수 차이를 0으로
치환하지 않는다. 예를 들어 ADDED의 beforeCenter, beforeRadiusMeters, beforePriorityScore,
centerShiftMeters, scoreDelta는 모두 null이다.

### Frontend 표시 및 fallback

- priorityScore는 발견 확률이 아닌 수색 우선점수다. 퍼센트나 정확도 개선율로 표시하지 않는다.
- V1/V2 점수 차이는 정확도 향상의 증거가 아니다.
- centerShiftMeters는 **동일 순위 간 중심점 거리**이며 동일 순위는 동일 물리적 영역이 아니다.
  동물의 이동 경로나 같은 장소의 순위 변화로 해석하지 않는다.
- reasonCodes 변화는 설명 체계의 차이에서도 발생하며 환경 변화의 증거가 아니다.
  사용자에게는 reason, assumptions, comparison.summary를 우선 표시한다.
- V1 LOW_TRAFFIC은 실제 차량 통행량을 의미하지 않는다.
- fallbackUsed=true도 HTTP 200이다. 환경 조회 실패 또는 유효 후보 부족 시 fallback을 사용할 수 있다.
  scoreBreakdown=null이면 기여점수 차트를 숨기고 환경 정보 부족을 안내하며 V2 개선 효과를 주장하지 않는다.
  null을 빈 객체나 0점으로 변환하지 않는다.

### 비교 API 오류

| HTTP | 조건 |
|---|---|
| 401 | 인증 정보 없음 |
| 403 | 작성자 또는 ADMIN이 아님 |
| 404 | 신고 없음 |
| 422 | LOST 이외 / 강아지 이외 신고 또는 AI 422 |
| 502 | AI 기타 4xx/5xx, JSON 파싱 실패, 빈 응답, reportId 불일치, 필수 계약 누락/위반 |
| 503 | AI 연결 실패 또는 timeout |

비교 응답의 최상위 필드, 버전, 영역/순위 비교 목록, 영역별 필수 필드와 정상 결과의
기여점수 존재·범위·합계를 검증한다. fallback의 null 기여점수는 허용한다.
오류 메시지에 AI 주소, 인증 정보, 원본 예외를 포함하지 않는다. 기존과 동일하게
ResponseStatusException을 사용하고 새 오류 래퍼를 도입하지 않는다.

### AI 미가동 시 테스트와 배포 후 확인

```bash
cd backend
./gradlew clean test --no-daemon
```

JUnit 5, Mockito, MockMvc, MockRestServiceServer로 검증하므로 실제 AI 서버는 필요 없다.
fixture는 AI 저장소의 실제 요청/응답 예시를 복사한 것이며 외부 HTTP는 Mock으로 대체한다.
정상/잘못된 응답, 인증 및 신고 조건, 오류 변환, fallback null, offset, V1 호환성을 검증한다.

실제 AI 서버/Overpass/운영 서버 네트워크 통합 테스트는 이번 작업에 포함하지 않는다.
서버 재가동 후 배포 환경에서는 다음을 확인한다.

1. 동일 AI Base URL에 /search-areas와 /search-areas/compare가 모두 제공되는지 확인한다.
2. 실제 JWT로 작성자/ADMIN 성공 및 비인증/타인 거부를 확인한다.
3. 동일 신고의 기존 V1 응답과 비교 baseline의 기존 필드가 일치하는지 확인한다.
4. 실서비스 환경 결과의 전체 계약, offset, fallback 및 명시적 null을 확인한다.
5. 실제 지도에서 중심·반경, 순위별 비교 및 추가/제거 표시를 확인한다.
6. 정상/환경 조회 지연 시 처리 시간을 측정한다. connect 2000ms / read 15000ms는 유지했으며,
   실측 후 timeout 조정 필요성을 판단한다. 비교 계산이 길다는 이유만으로 사전 확대하지 않는다.
