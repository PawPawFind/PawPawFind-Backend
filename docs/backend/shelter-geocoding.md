# 보호소 위치 지오코딩

## 목적

공공 유기동물의 보호소명·주소·전화번호와 지도 좌표를 `shelter_locations`에 보호소 단위로
저장한다. 동물마다 좌표를 중복 저장하지 않는다. 이번 범위는 위치 데이터 구축까지이며
`GET /api/reports/{reportId}/matches` 응답 확장은 후속 Issue에서 진행한다.

## 보호소 식별과 주소 정규화

- `careRegNo`와 `careAddr`의 앞뒤 공백을 제거한다.
- 주소 안의 연속 공백은 하나로 축소한다.
- 등록번호가 있으면 `REG:{careRegNo}`를 shelterKey로 사용한다.
- 등록번호가 없으면 정규화 주소의 UTF-8 SHA-256으로 `ADDR:{hash}`를 만든다.
- 등록번호와 주소가 모두 없으면 위치 레코드를 만들지 않는다.
- `careAddr` 원문과 `normalizedAddress`를 구분해 저장한다.

같은 주소를 다시 받으면 기존 좌표와 성공 상태를 유지하면서 보호소명·전화번호만 갱신한다.
등록번호 기반 보호소의 주소 해시가 바뀌면 좌표, provider, 성공 시각과 시도 횟수를 초기화하고
`PENDING`으로 되돌린다.

## 상태와 재시도

```text
신규/주소 변경 ──▶ PENDING
PENDING/FAILED ──▶ SUCCESS       좌표 저장, provider=KAKAO
               ├─▶ NOT_FOUND     검색 결과 없음, 자동 재호출하지 않음
               └─▶ FAILED        연결·timeout·429·5xx는 최대 횟수까지 재시도
```

그 외 4xx와 유효하지 않은 좌표는 재시도 불가능 실패로 처리해 자동 시도 한도를 소진한다.
한 실행은 `batchSize`개까지만 조회하고 `maxAttempts`에 도달한 실패는 제외한다. JVM 내부 실행
가드로 수동 동기화와 스케줄 동기화가 겹칠 때 같은 프로세스의 중복 batch 실행을 막는다.

## Kakao Local API 설정

```properties
kakao.rest-api-key=YOUR_KAKAO_REST_API_KEY
kakao.local.enabled=false
kakao.local.base-url=https://dapi.kakao.com
kakao.local.connect-timeout-millis=2000
kakao.local.read-timeout-millis=5000
kakao.local.batch-size=50
kakao.local.max-attempts=3
```

REST API 키는 소스나 Git에 저장하지 않고 운영 설정 파일 또는 비밀 관리 수단에 둔다. Local API는
기본 비활성화다. 운영에서 키와 카카오 Local API 사용 권한을 확인한 뒤 `enabled=true`로 전환한다.
활성화 상태에서 키가 비어 있거나 URL·timeout·batch·시도 횟수가 유효하지 않으면 애플리케이션
시작 단계에서 설정 오류가 발생한다.

호출 계약은 `GET /v2/local/search/address.json`, `Authorization: KakaoAK {REST_KEY}`이며 query는
URI 빌더가 인코딩한다. 첫 번째 유효한 document의 `x`를 경도, `y`를 위도로 사용한다. 응답 본문,
API 키, 내부 URL과 예외 상세는 로그에 기록하지 않는다.

## 공공데이터 동기화와 백필

수동 `GET /abandoned`와 1시간 스케줄은 동일한 `AbandonedService` 흐름을 사용한다.

1. Animal 저장
2. 기존 embedding 트리거
3. 동기화 내 shelterKey 중복 제거 후 위치 upsert
4. 전체 동기화가 끝나면 제한된 지오코딩 batch 한 번 실행

Local API가 비활성화돼도 위치 레코드는 `PENDING`으로 생성된다. 기존 `animals`도 이후 공공데이터
동기화에 다시 등장할 때 점진적으로 위치 레코드가 만들어지므로 전체 테이블을 한 번에 메모리에
올리는 공개 백필 API는 두지 않는다. 위치 upsert나 외부 API 한 건이 실패해도 다른 Animal 저장과
embedding 흐름은 계속된다.
