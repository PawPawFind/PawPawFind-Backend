# 포포파인드 DB 스키마

| 항목 | 내용 |
|------|------|
| 문서 버전 | 1.0 (제보 v6 기준) |
| 관련 문서 | [기능 명세서](./backend-기능명세서.md) · [BE 할 일](./backend-할일.md) |
| 현재 DB | H2 in-memory `jdbc:h2:mem:pawpawfind` / 사용자 `sa` / 비밀번호 없음 |
| 테이블 생성 | JPA `ddl-auto=update` (엔티티가 곧 스키마) |

서버를 끄거나 `bootRun`을 재시작하면 H2 데이터는 사라진다.

---

## 1. 테이블 구분

| 테이블 | 출처 | 용도 | FK |
|--------|------|------|----|
| `animals` | 공공 API | 보호소 공고 | 없음 |
| `reports` | 사용자 | 실종/목격 제보 | `user_id`는 숫자만. `users` 테이블 없음 |
| `report_photos` | 사용자 | 제보 사진 URL | `report_id` (DB FK 제약 아직 없음) |
| `report_features` | 사용자 | 외형 태그 | `report_id` (DB FK 제약 아직 없음) |

매칭 시 AI는 `animals`와 `reports`를 둘 다 후보로 쓸 수 있다. 두 테이블을 JOIN하지 않는다.

```text
animals          reports ──┬── report_photos
                           └── report_features
```

---

## 2. `animals` — 공공 보호 공고

PK는 우리가 만들지 않는다. 공공 `desertionNo`를 그대로 쓴다.

입양(`adptn*`)·봉사(`srvc*`)·`vaccinationChk` / `healthChk` / `careOwnerNm` 은 저장하지 않는다.

| 컬럼 | 타입 | API 필드 | 설명 |
|------|------|----------|------|
| desertion_no | VARCHAR(20) PK | desertionNo | 개체 고유 ID |
| happen_dt | VARCHAR(8) | happenDt | 발견일 YYYYMMDD |
| happen_place | VARCHAR(255) | happenPlace | 발견 장소 |
| up_kind_cd | VARCHAR(10) | upKindCd | 축종 코드 (개 417000 등) |
| up_kind_nm | VARCHAR(20) | upKindNm | 축종명 |
| kind_cd | VARCHAR(10) | kindCd | 품종 코드 |
| kind_nm | VARCHAR(100) | kindNm | 품종명 |
| kind_full_nm | VARCHAR(150) | kindFullNm | 표시용 전체명 |
| color_cd | VARCHAR(50) | colorCd | 색상 |
| age | VARCHAR(50) | age | 나이 |
| weight | VARCHAR(50) | weight | 체중 |
| notice_no | VARCHAR(50) | noticeNo | 공고번호 |
| notice_sdt | VARCHAR(8) | noticeSdt | 공고 시작 |
| notice_edt | VARCHAR(8) | noticeEdt | 공고 종료 |
| popfile1 | TEXT | popfile1 | 사진1 URL |
| popfile2 | TEXT | popfile2 | 사진2 URL |
| process_state | VARCHAR(30) | processState | 보호중 등 |
| sex_cd | CHAR(1) | sexCd | M / F / Q |
| neuter_yn | CHAR(1) | neuterYn | Y / N / U |
| special_mark | TEXT | specialMark | 특징 |
| care_reg_no | VARCHAR(30) | careRegNo | 보호소 등록번호 |
| care_nm | VARCHAR(100) | careNm | 보호소명 |
| care_tel | VARCHAR(30) | careTel | 보호소 전화 |
| care_addr | VARCHAR(255) | careAddr | 보호소 주소 |
| org_nm | VARCHAR(100) | orgNm | 관할 시군구 |
| source_upd_tm | VARCHAR(30) | updTm | 공공 원본 갱신시각 |
| updated_at | TIMESTAMP | (우리) | DB 반영 시각 — `@PrePersist` 없음 |
| created_at | TIMESTAMP | (우리) | 최초 적재 시각 — `@PrePersist` 없음 |

**인덱스 제안 (아직 없음)**

- `updated_at` / `source_upd_tm` — 증분 동기화·AI
- `up_kind_cd`, `kind_cd`
- `notice_edt`, `process_state`

동기화 시 `desertion_no`가 있으면 update, 없으면 insert가 목표. 지금은 `save`만 한다.

---

## 3. `reports` — 사용자 실종/목격

엔티티: `Reports.java`. JSON은 camelCase (`eventDate`).

| 컬럼 | 타입 | 필수 | 설명 |
|------|------|------|------|
| report_id | BIGINT PK IDENTITY | 자동 | 제보 ID |
| user_id | BIGINT | N | 작성자. `users` 연동 전 null |
| report_type | VARCHAR(10) | Y | `LOST` / `FOUND` |
| title | VARCHAR(255) | N | FOUND만 사용 |
| species | VARCHAR(20) | Y | 강아지 / 고양이 |
| size | VARCHAR(10) | Y | 소형 등 |
| color | VARCHAR(20) | Y | 색상 |
| event_date | DATE | Y | 실종일 또는 목격일 |
| event_hour | INT | N | 0~23. 모르면 null |
| happen_place | VARCHAR(255) | Y | 장소 텍스트 |
| latitude | DOUBLE | Y | 위도 |
| longitude | DOUBLE | Y | 경도 |
| description | TEXT | Y | 설명 |
| status | VARCHAR(20) | Y | 기본 `OPEN`. `CLOSED`는 종료 |
| created_at | TIMESTAMP | 자동 | `@PrePersist` |
| updated_at | TIMESTAMP | 자동 | `@PrePersist` / `@PreUpdate` |

**넣지 않는 것 (팀 표와 차이)**

| 필드 | 이유 |
|------|------|
| breed | 제보 본문에 없음. 특징 태그로 대체 가능 |
| sex | 제보 본문에 없음 |
| feature_tags JSON | `report_features` 테이블로 분리 |
| happen_date / happen_hour | `event_date` / `event_hour`로 이름 확정 |
| contact_name / contact_phone | 회원·연락처는 후순위 |

---

## 4. `report_photos`

파일 바이너리는 저장하지 않는다. URL만.

| 컬럼 | 타입 | 필수 | 설명 |
|------|------|------|------|
| id | BIGINT PK IDENTITY | 자동 | |
| report_id | BIGINT | Y | 제보 ID. JPA `@ManyToOne` / DB FK 없음 |
| photo_url | TEXT | Y | 이미지 URL |
| sort_order | INT | N | 표시 순서 |
| created_at | TIMESTAMP | 자동 | |
| updated_at | TIMESTAMP | 자동 | 스키마 v6에는 없을 수 있음. 엔티티에 있음 |

제보를 지우면 사진 행은 자동으로 안 지워진다 (cascade 없음).

---

## 5. `report_features`

한 제보에 태그 여러 행.

| 컬럼 | 타입 | 필수 | 설명 |
|------|------|------|------|
| id | BIGINT PK IDENTITY | 자동 | |
| report_id | BIGINT | Y | 제보 ID. DB FK 없음 |
| category | VARCHAR(30) | Y | 예: 털색 |
| keyword | VARCHAR(50) | Y | 예: 흰색 |

---

## 6. 아직 없는 테이블

| 테이블 | 언제 |
|--------|------|
| `users` | 소셜 로그인 (1.x) |
| 매칭 결과 / 점수 | AI 연동 (4.x) |
| embedding / 벡터 | 6.5 |
| 알림 | 7.x |
| 후기/댓글 | 10.x |

---

## 7. 로컬 확인

- H2 콘솔: `http://127.0.0.1:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:pawpawfind`
- User: `sa` / Password: (비움)

운영 DB(PostgreSQL 등)로 바꿀 때는 `application.properties`의 datasource만 바꾸고, `ddl-auto`는 운영에서 `update`를 쓰지 않는 편이 안전하다.
