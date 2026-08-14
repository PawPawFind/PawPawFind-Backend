# 백엔드 할 일 (강유진)

| 항목 | 내용 |
|------|------|
| 문서 버전 | 1.0 |
| 관련 문서 | [기능 명세서](./backend-기능명세서.md) · [DB](./database.md) |
| 기준일 | 2026-08-15 |

끝난 것과 남은 것만 적는다. 팀 기능 ID는 괄호로 표시.

---

## 이미 된 것

- 공공 공고 수집: `GET /abandoned` → 공공 API `state=notice`, 페이지 1000, `URI.create`로 키 이중 인코딩 방지 (6.1)
- `animals` 테이블에 저장 (6.2)
- 제보 CRUD `/api/reports` — LOST/FOUND, eventDate, 좌표, 설명 (2.1, 2.4, 2.5)
- 제보 사진 URL CRUD `/api/report-photos`
- 제보 특징 태그 CRUD `/api/report-features` (2.3, 2.7)
- Swagger UI (`/swagger-ui.html`)
- H2 메모리 DB로 로컬 저장

---

## 지금 고칠 것 (버그·빠진 동작)

우선순위 위부터.

| # | 할 일 | 이유 |
|---|--------|------|
| 1 | 사진 목록을 `reportId`로 필터 | `GET /api/report-photos`가 지금 전체 사진을 준다. 특징 API는 이미 필터함 |
| 2 | `ReportPhotoRepository.findByReportId` 추가 | 위 필터에 필요 |
| 3 | 제보 삭제 시 사진·특징도 삭제 | 지금 cascade/수동 삭제 없음 |
| 4 | 없는 ID 조회 시 404 | 지금은 `null` body |
| 5 | `Animal.createdAt` / `updatedAt` 자동 세팅 | `@PrePersist` 없음 |
| 6 | API 키를 Git에 안 올리기 | `application.properties` → 환경변수 또는 `.gitignore` |
| 7 | `AbandonedController` 패키지를 `controller`로 맞추기 | 파일은 controller 폴더, 패키지는 `com.pawpawfind.backend` |

---

## 다음에 만들 것 — 공공 공고 (6.x)

목표: 사용자는 DB만 읽고, 공공 API는 스케줄만 호출.

| # | 할 일 | 기능 ID |
|---|--------|---------|
| 1 | `@Scheduled`로 주기 동기화 (예: 6시간) | 6.3 |
| 2 | `GET /abandoned`를 동기화에서 분리. 조회는 `GET /api/animals` | 6.3 |
| 3 | `GET /api/animals/{desertionNo}` 상세 + 없으면 404 | 4.4 |
| 4 | 목록 페이지네이션·필터 (축종, 지역, 상태) | |
| 5 | upsert: 같은 `desertionNo`면 변경 필드만 update | 6.3 |
| 6 | `GET /api/animals/changes?since=` AI용 증분 | 6.5 준비 |
| 7 | 공공 호출 코드를 `AnimalPublicApiClient`로 이동 | 정리 |

---

## 다음에 만들 것 — 제보 (2.x)

| # | 할 일 | 기능 ID |
|---|--------|---------|
| 1 | 목록 최신순 + 페이지네이션 | 2-1 |
| 2 | 필터: `reportType`, 기간, 종류, 색상 | 2-1 |
| 3 | 사진 1~3장 개수 제한 | 2.2 / 2.6 |
| 4 | FE와 사진 URL 전달 방식 확정 (업로드는 FE+AI) | 2.2 |
| 5 | 필요하면 `breed` / `sex` 컬럼 추가 — 팀과 확인 | 2.1 |

---

## FE / AI와 맞춘 뒤

| # | 할 일 | 기능 ID |
|---|--------|---------|
| 1 | 매칭 후보 조회 API (보호소 + 제보) | 4.1 ~ 4.5 |
| 2 | AI 점수 저장 포맷 협의 | 3.x / 4.3 |
| 3 | 지도: 제보 좌표 목록, 주변 반경 검색 | 5.1 ~ 5.5 |
| 4 | Embedding 저장 필드/API | 6.5 |

---

## 후순위

| # | 할 일 | 기능 ID |
|---|--------|---------|
| 1 | `users` + 소셜 로그인, 제보에 `user_id` 연결 | 1.1 ~ 1.4 |
| 2 | 내 제보만 조회 | 1.4 |
| 3 | 알림 | 8.1, 8.2 |
| 4 | 후기/커뮤니티 | 11.x |
| 5 | H2 → PostgreSQL 등 실제 DB | 배포 |
| 6 | 이미지 스토리지(S3) — URL만 받기로 하면 생략 가능 | |
| 7 | EC2 배포, 스케줄이 서버 꺼져도 돌게 | |

---

## 권장 순서 (혼자 개발할 때)

1. 사진 `reportId` 필터 + 404 (작게 고치기)
2. 공고 조회 API를 DB 읽기로 분리 (`GET /api/animals`)
3. `@Scheduled` 동기화 (6.3)
4. 제보 목록 페이지·필터 (2-1)
5. AI/FE 스펙 맞춘 뒤 매칭·지도 API

회원·알림은 위가 끝난 다음.
