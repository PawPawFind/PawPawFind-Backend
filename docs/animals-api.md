# 보호소 공고 · 공공데이터 동기화 API

| 항목 | 내용 |
|------|------|
| 담당 | **강유진** |
| 목차 | [BACKEND.md](../BACKEND.md) |

보호소(공공) 공고는 `animals` 테이블에 저장한다. PK는 공공 `desertionNo`다.

---

## 조회

### `GET /api/animals`

인증 없음. DB에 있는 공고 **전체** 리스트(`Animal` 엔티티 JSON).

### `GET /api/animals/{desertionNo}`

인증 없음. 없으면 404.

응답은 `Animal` 필드 기준이다. 예: `desertionNo`, `happenDt`, `happenPlace`, `kindNm`, `colorCd`, `popfile1`, `processState`, `careNm`, `careAddr`, `careTel`, …

일상 화면 조회는 이 API를 쓴다.

---

## 수동 동기화

### `GET /abandoned`

인증 없음. `/api` 경로가 아니므로 `WebConfig` CORS(`/api/**`) 대상이 **아니다**.

공공 API를 `state=notice`, `numOfRows=1000`으로 호출해 DB에 upsert한 뒤, 동기화 결과(저장된 목록)를 반환한다.

| 설정 | 용도 |
|------|------|
| `animal.api.key` | 공공 API 서비스키 |
| `animal.api.url` | abandonmentPublic_v2 endpoint |

주기 동기화: `AbandonedService` `@Scheduled(fixedRate = 1시간)`이 동일 sync를 호출한다.

동기화 후:

- `ShelterLocation` upsert (보호소 키 기준)
- `kakao.local.enabled=true`이면 주소 지오코딩 배치
- 임베딩이 없는 공고는 AI `POST {ai}/embed/animal` 비동기 트리거

---

## 오류

| 상태 | 조건 |
|------|------|
| 404 | `GET /api/animals/{desertionNo}` 해당 공고 없음 |
| 5xx | 공공 API 장애·파싱 실패 시 동기화 예외 |

공통 에러 JSON 래퍼는 없다.
