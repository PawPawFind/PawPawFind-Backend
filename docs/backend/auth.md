# 인증 · 권한

기준일: 2026-08-25.

Spring Security `SecurityFilterChain`은 쓰지 않는다. `JwtAuthFilter`가 Bearer 토큰을 **소프트 파싱**해 request attribute(`userId`, `role`)만 세팅하고, 엔드포인트별로 서비스가 401/403을 낸다.

## 로그인 흐름

```text
FE Kakao OAuth → authorization code
  → POST /api/auth/kakao { "code": "..." }
  → 카카오 토큰 + /v2/user/me
  → users upsert (provider=KAKAO, provider_id, nickname, role)
  → JWT 발급
  → AuthResponse { accessToken, userId, nickname, provider, role }
```

현재 **카카오만** 구현. (엔티티 주석의 GOOGLE은 미구현)

## JWT

| 항목 | 내용 |
|------|------|
| 알고리즘 | HS256 |
| Subject | `userId` |
| Claim | `role` |
| 시크릿 | `jwt.secret` |
| 만료 | `jwt.expiration-ms` (기본 86400000 = 24h) |

호출 시 헤더:

```http
Authorization: Bearer <accessToken>
```

Swagger Authorize에도 동일 스키마가 등록되어 있다.

## 역할

| role | 부여 |
|------|------|
| `USER` | 기본 |
| `ADMIN` | `admin.kakao-ids`에 포함된 카카오 `provider_id`로 로그인할 때 |

```properties
# 쉼표 구분. 확인: 로그인 후 users.provider_id
admin.kakao-ids=1234567890,9876543210
```

재로그인 시 목록에 있으면 `ADMIN`, 없으면 `USER`로 갱신된다.

## API별 규칙

| 동작 | 규칙 |
|------|------|
| `POST /api/reports` | JWT 있으면 `userId` 저장, 없어도 생성 가능 |
| `POST /api/report-photos`, `POST /api/report-features` | Public (목격 제보 등 비로그인 작성 흐름) |
| `GET /api/reports/me` | JWT 필수 |
| 제보·사진·특징 **수정/삭제**, `search-areas` | JWT + **작성자 또는 ADMIN** |
| 그 외 조회·nearby·run-match 등 | Public |
| `/api/internal/**` | 인증 없음 (네트워크로 제한) |

`userId`가 null인 익명 제보는 일반 USER가 관리할 수 없다. ADMIN만 가능.

## 설정 키

```properties
kakao.rest-api-key=...
kakao.redirect-uri=http://localhost:5173/callback
admin.kakao-ids=
jwt.secret=...
jwt.expiration-ms=86400000
```

→ [configuration.md](./configuration.md)
