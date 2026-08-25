# 추천 수색 영역 백엔드 연동 API

| 항목 | 내용 |
|------|------|
| 담당 | **강유진** |
| 목차 | [docs/backend/](./backend/README.md) · [AI 연동](./backend/ai-integration.md) · [담당 전체](./backend/contributors.md) |

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
