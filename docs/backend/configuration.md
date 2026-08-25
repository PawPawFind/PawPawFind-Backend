# 설정

기준일: 2026-08-25.

| 환경 | 파일 |
|------|------|
| 로컬 예시 | `backend/src/main/resources/application.properties.example` → `application.properties` (gitignore) |
| 운영 예시 | `deploy/application-prod.properties.example` → EC2 `~/application-prod.properties` (Git 금지) |

시크릿·API 키는 커밋하지 않는다.

## 키 목록

| 키 | 용도 | 비고 |
|----|------|------|
| `spring.datasource.*` | DB | 로컬 H2 / 운영 PostgreSQL |
| `spring.jpa.hibernate.ddl-auto` | 스키마 | 보통 `update` |
| `spring.jpa.database-platform` | 방언 | 운영 PostgreSQL |
| `animal.api.key` | 공공 유기동물 API 인증 | 인코딩키 |
| `animal.api.url` | 공공 API URL | |
| `kakao.rest-api-key` | 카카오 REST | |
| `kakao.redirect-uri` | OAuth redirect | FE 콜백과 일치 |
| `admin.kakao-ids` | ADMIN 카카오 `provider_id` | 쉼표 구분 |
| `jwt.secret` | JWT HS256 | 32자+ 권장 |
| `jwt.expiration-ms` | JWT 만료(ms) | 기본 86400000 |
| `ai.service.url` | AI base URL | 운영은 private IP |
| `ai.service.connect-timeout-millis` | AI 연결 timeout | 기본 2000 |
| `ai.service.read-timeout-millis` | AI 읽기 timeout | 기본 15000 (Overpass 10s보다 길게) |
| `aws.region` | S3 리전 | |
| `aws.s3.bucket` | 버킷 | |
| `aws.s3.upload-prefix` | 객체 prefix | 예: `reports/` |

## 로컬 최소 예

```properties
spring.application.name=backend
spring.datasource.url=jdbc:h2:mem:pawpawfind
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
spring.h2.console.enabled=true

animal.api.key=YOUR_ENCODING_KEY
animal.api.url=https://apis.data.go.kr/1543061/abandonmentPublicService_v2/abandonmentPublic_v2

kakao.rest-api-key=YOUR_KAKAO_REST_API_KEY
kakao.redirect-uri=http://localhost:5173/callback
admin.kakao-ids=

jwt.secret=CHANGE_ME_TO_A_LONG_RANDOM_SECRET

ai.service.url=http://127.0.0.1:8000
ai.service.connect-timeout-millis=2000
ai.service.read-timeout-millis=15000

aws.region=ap-northeast-2
aws.s3.bucket=pawpawfind-report-photos
aws.s3.upload-prefix=reports/
```

## 운영 참고

- BE EC2와 AI EC2는 같은 VPC → `ai.service.url=http://<AI_PRIVATE_IP>:8000`
- RDS PostgreSQL URL·계정은 EC2 로컬 properties에만
- 배포: `.github/workflows/deploy-backend.yml` · [GITHUB_ACTIONS.md](../../deploy/GITHUB_ACTIONS.md)

## Swagger

기동 후 `http://127.0.0.1:8080/swagger-ui.html`  
Authorize에 Bearer JWT를 넣으면 Owner/ADMIN API를 UI에서 호출할 수 있다.
