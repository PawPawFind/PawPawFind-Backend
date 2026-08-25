# Backend DB 스키마

| 항목 | 내용 |
|------|------|
| 기준일 | 2026-08-25 |
| 로컬 | H2 in-memory `jdbc:h2:mem:pawpawfind` |
| 운영 | PostgreSQL (RDS) |
| 스키마 | JPA `ddl-auto=update` (엔티티 ≈ 스키마) |

관련: [README](./README.md) · [api.md](./api.md)  
레거시 초안: [../database.md](../database.md) (users·임베딩·매칭 이전)

---

## ER 개요

```text
users
  └── reports.user_id          (논리 연결, JPA FK 없음)

reports
  ├── report_photos
  │     └── report_embeddings  (report_photo_id unique)
  ├── report_features
  └── match_runs
        └── match_results      (SHELTER → desertion_no / REPORT → candidate_report_id)

animals (PK = desertion_no)
  └── animal_embeddings        (gallery_id unique)
```

자식 테이블은 `report_id` 등을 컬럼으로만 들고, DB FK 제약은 두지 않은 경우가 많다.

---

## `users`

소셜 로그인 사용자.

| 컬럼 | 설명 |
|------|------|
| `user_id` | PK |
| `provider` | `KAKAO` (GOOGLE 미구현) |
| `provider_id` | 카카오 회원번호 |
| `nickname` | 닉네임 |
| `role` | `USER` \| `ADMIN` |
| `created_at`, `updated_at` | |

유니크: `(provider, provider_id)`

---

## `reports`

실종(`LOST`) / 목격(`FOUND`) 제보.

| 컬럼 | 설명 |
|------|------|
| `report_id` | PK |
| `user_id` | 작성자 (nullable — 익명 가능) |
| `report_type` | `LOST` \| `FOUND` |
| `title` | FOUND용 |
| `species`, `size` | 종·크기 |
| `event_date`, `event_hour` | 일자·시간(0–23, null 가능) |
| `happen_place` | 장소 문자열 |
| `latitude`, `longitude` | 좌표 |
| `description` | 설명 |
| `status` | 기본 `OPEN` |
| `created_at`, `updated_at` | |

---

## `report_photos`

| 컬럼 | 설명 |
|------|------|
| `id` | PK |
| `report_id` | 제보 |
| `photo_url` | S3 등 URL |
| `sort_order` | 썸네일·표시 순서 (작을수록 우선) |
| `created_at`, `updated_at` | |

제보당 최대 3장 (서비스 검증).

---

## `report_features`

외형·행동 태그. `category` + `keyword` (털색은 색상당 1행, 최대 3).

| 컬럼 | 설명 |
|------|------|
| (PK id) | |
| `report_id` | |
| `category` | 예: `털색`, `활동량`, `낯선사람반응` … |
| `keyword` | 예: `갈색`, `HIGH` |
| timestamps | |

행동 카테고리 → AI search-areas 매핑: [search-area-api.md](../search-area-api.md)

---

## `animals`

공공 보호소 공고. PK = 공공 `desertionNo`.

주요 컬럼: `happen_dt`, `happen_place`, 축종/품종(`up_kind_*`, `kind_*`), `color_cd`, `age`, `weight`, 공고기간, `popfile1`/`popfile2`, `process_state`, `sex_cd`, `neuter_yn`, `special_mark`, 보호소(`care_*`), `org_nm`, `source_upd_tm`, timestamps.

입양·봉사 관련 공공 필드는 저장하지 않는다.

---

## `animal_embeddings`

보호소 사진 Re-ID 갤러리 (AI batch 적재).

| 컬럼 | 설명 |
|------|------|
| `id` | PK |
| `desertion_no` | 공고 |
| `gallery_id` | unique |
| `species`, `photo_index` | |
| `image_url` | |
| `phash_full`, `phash_crop` | |
| `model_version`, `preprocess_version` | |
| `embedding_ref` | 벡터 참조/직렬화 |
| `detection_confidence`, `blur_score` | |
| timestamps | |

---

## `report_embeddings`

제보 사진별 임베딩.

| 컬럼 | 설명 |
|------|------|
| `id` | PK |
| `report_photo_id` | unique |
| `report_id` | |
| `model_version`, `preprocess_version` | |
| `phash_*`, `embedding_ref` | |
| timestamps | |

---

## `match_runs` / `match_results`

한 번의 매칭 실행과 Top-N 후보.

**match_runs:** `report_id`, `model_version`, `rerank_version`, `decision`, `status`, `created_at` 등

**match_results:** `match_run_id`, `rank`, `candidate_type` (`SHELTER` \| `REPORT`),

- SHELTER → `desertion_no` 필수, `candidate_report_id` null
- REPORT → `candidate_report_id` 필수, `desertion_no` null

점수: `visual_score`, `ranking_score`, `tag_score`, `text_score`, `phash_distance`, `near_duplicate`, 태그 JSON, `gallery_id`, `image_url`

---

## 주변 검색

PostGIS 없이 bounding box 후보 + Java Haversine. 상세: [nearby-reports-api.md](../nearby-reports-api.md)
