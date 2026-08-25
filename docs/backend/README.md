# PawPawFind Backend

| 항목 | 내용 |
|------|------|
| 스택 | Spring Boot · JPA · H2(로컬) / PostgreSQL(운영) |
| 기본 URL | 로컬 `http://127.0.0.1:8080` |
| Swagger | `http://127.0.0.1:8080/swagger-ui.html` |
| 기준일 | 2026-08-25 |
| 담당 | [강유진 · 김가윤](./contributors.md) |

실종·목격 제보, 보호소 공고, 카카오 로그인, AI 매칭·수색 영역·임베딩 연동을 담당한다.  
개발자별 범위는 [contributors.md](./contributors.md).

## 문서 목차

| 문서 | 내용 |
|------|------|
| [contributors.md](./contributors.md) | 누가 무엇을 개발했는지 |
| [api.md](./api.md) | HTTP API 전체 목록 · 인증 · 주요 필드 |
| [auth.md](./auth.md) | 카카오 로그인 · JWT · USER/ADMIN |
| [database.md](./database.md) | 테이블 · 관계 |
| [ai-integration.md](./ai-integration.md) | BE ↔ AI 호출 · internal API |
| [configuration.md](./configuration.md) | 설정 키 · 로컬/운영 |
| [shelter-geocoding.md](./shelter-geocoding.md) | 보호소 위치 저장 · 카카오 주소 지오코딩 |
| [match-shelter-location.md](./match-shelter-location.md) | 매칭 후보 보호소 정보 · 좌표 응답 |
| [../nearby-reports-api.md](../nearby-reports-api.md) | 주변 제보 상세 (담당: 김가윤) |
| [../search-area-api.md](../search-area-api.md) | 추천 수색 영역 상세 (담당: 김가윤) |

레거시(팀 기능표·할 일): [backend-기능명세서.md](../backend-기능명세서.md) · [backend-할일.md](../backend-할일.md) — 과거 스냅샷. **현재 구현 기준은 이 폴더**를 본다.

## 한눈에 보는 흐름

```text
FE ──JWT──▶ Backend ──▶ AI (match / search-areas / embed)
              │              ▲
              │              │ gallery · embeddings · match-results
              └──────────────┘  /api/internal/*
```

1. **카카오 로그인** → JWT 발급 (`role`: USER 또는 ADMIN)
2. **제보 CRUD** + S3 사진 URL + 특징 태그
3. **주변 제보** · **매칭 결과** · **수색 영역** 조회/실행
4. **보호소 공고** 주기 동기화 + 임베딩 트리거
5. AI는 **internal API**로 갤러리·임베딩·매칭 결과를 읽고 쓴다

## 인증 요약

| 구분 | 방식 |
|------|------|
| 공개 | 목록·상세·nearby·제보/사진/특징 **생성**·건강체크 등 |
| JWT 필요 | 내 제보, 제보/사진/특징 **수정·삭제**, search-areas |
| 권한 | 작성자(`userId`) 또는 `ADMIN` |
| Internal | `/api/internal/**` — 앱 단 토큰 없음 (VPC/네트워크 신뢰) |

자세한 표는 [api.md](./api.md), 로그인·역할은 [auth.md](./auth.md).

## 로컬 실행

```bash
cd backend
cp src/main/resources/application.properties.example \
   src/main/resources/application.properties
# 키·시크릿 채운 뒤
./gradlew bootRun
```

운영 설정 예시는 `deploy/application-prod.properties.example`.
