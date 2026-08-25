# 카카오 로그인 API

| 항목 | 내용 |
|------|------|
| 담당 | **강유진** |
| 목차 | [BACKEND.md](../BACKEND.md) |

## 호출

`POST /api/auth/kakao`

별도 JWT 없이 호출한다. 프론트엔드가 카카오 OAuth로 받은 **인가 코드**(`code`)를 전달하면,
백엔드가 카카오 토큰·프로필을 조회한 뒤 `users`를 upsert하고 JWT를 발급한다.

| 필드 | 필수 | 설명 |
|---|---:|---|
| code | 예 | 카카오 authorization code |

## 요청 예시

```json
{
  "code": "xxxxxxxx"
}
```

## 응답 예시

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "userId": 1,
  "nickname": "포포",
  "provider": "KAKAO",
  "role": "USER"
}
```

| 필드 | 설명 |
|------|------|
| accessToken | HS256 JWT. 이후 `Authorization: Bearer <accessToken>` |
| userId | `users.user_id` |
| nickname | 카카오 프로필 닉네임 |
| provider | 고정 `"KAKAO"` |
| role | `"USER"` 또는 `"ADMIN"` |

## 처리 흐름

1. `kakao.rest-api-key`, `kakao.redirect-uri`로 `https://kauth.kakao.com/oauth/token`에 토큰 요청
2. access token으로 `https://kapi.kakao.com/v2/user/me` 조회
3. `(provider, provider_id)`로 사용자 찾거나 생성. 닉네임·role 갱신
4. `admin.kakao-ids`(쉼표 구분 카카오 회원번호)에 `provider_id`가 있으면 `ADMIN`, 아니면 `USER`
5. JWT subject=`userId`, claim `role`, 만료 `jwt.expiration-ms`(기본 24시간)

## 설정

```properties
kakao.rest-api-key=
kakao.redirect-uri=http://localhost:5173/callback
admin.kakao-ids=
jwt.secret=
jwt.expiration-ms=86400000
```

`kakao.redirect-uri`는 카카오 개발자 콘솔·프론트 콜백과 **완전히 동일**해야 한다.
운영 HTTPS 사이트면 redirect도 HTTPS여야 한다.

## 오류

코드에 공통 에러 래퍼는 없다. 카카오 연동·파싱 실패 시 예외가 Propagate되어 Spring 기본 5xx/4xx로 보일 수 있다.
`code`가 null이면 카카오 토큰 요청이 실패한다.
