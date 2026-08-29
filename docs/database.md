# 데이터베이스 스키마

목차: [README.md](../README.md)

JPA 엔티티(`backend/src/main/java/com/pawpawfind/backend/entity/`)를 기준으로 정리한 문서다.
엔티티가 유일한 스키마 정의이므로, 이 문서와 코드가 어긋나면 코드가 맞다.

## 1. 환경

| 구분 | 값 |
|------|-----|
| 로컬 | H2 in-memory `jdbc:h2:mem:pawpawfind` |
| 테스트 | H2 in-memory `jdbc:h2:mem:pawpawfind-test`, `ddl-auto=create-drop` |
| 운영 | PostgreSQL (RDS), `PostgreSQLDialect` |
| 스키마 관리 | `spring.jpa.hibernate.ddl-auto=update` |

Flyway나 Liquibase를 쓰지 않는다. 스키마는 애플리케이션 기동 시 Hibernate가 엔티티에서 생성한다.

이 방식의 제약은 다음과 같다.

- 컬럼 추가는 자동 반영되지만, 컬럼 삭제·타입 변경·NOT NULL 완화는 반영되지 않는다.
- 그런 변경은 `deploy/migrations/` 에 SQL을 두고 수동으로 적용한다.
- 인덱스와 유니크 제약은 엔티티에 `@Index` / `@UniqueConstraint`로 선언한 것만 생성된다.

## 2. 설계 원칙

### 연관관계는 ID 컬럼으로 둔다

`@ManyToOne` / `@JoinColumn`을 쓰지 않고 `Long` 타입 ID 컬럼으로 연결한다.
따라서 **DB 레벨 외래키 제약이 없다.** 참조 무결성은 서비스 계층에서 보장한다.

지연 로딩으로 인한 의도치 않은 쿼리를 피하고 조회 범위를 명시적으로 통제하기 위한 선택이다.
대신 고아 레코드를 DB가 막아주지 않으므로, 삭제 로직에서 연관 레코드를 함께 정리해야 한다.

### 공공 데이터는 원본 PK를 그대로 쓴다

`animals` 테이블의 PK는 공공 API의 `desertionNo`다. 자체 시퀀스를 두지 않아
재동기화 시 upsert가 단순해진다.

### 파생 데이터는 별도 테이블로 분리한다

제보의 사진(`report_photos`), 외형 태그(`report_features`), 임베딩(`report_embeddings`)을
`reports`에서 분리했다. 개수가 가변이고 AI 파이프라인이 독립적으로 갱신하기 때문이다.

털색도 `reports`의 컬럼이 아니라 `report_features`의 한 행(`category=털색`)이다.
한 마리가 여러 색을 가질 수 있어서다.

## 3. 인덱스 및 제약

### 유니크 제약

| 테이블 | 제약명 | 컬럼 |
|--------|--------|------|
| `users` | `uk_users_provider` | `provider`, `provider_id` |
| `shelter_locations` | `uk_shelter_locations_shelter_key` | `shelter_key` |
| `match_results` | `uk_match_results_run_rank` | `match_run_id`, `rank` |
| `animal_embeddings` | (컬럼 단위 unique) | `gallery_id` |
| `report_embeddings` | (컬럼 단위 unique) | `report_photo_id` |

`uk_match_results_run_rank`는 한 매칭 실행에 같은 순위가 두 번 저장되는 것을 막는다.

### 인덱스

| 테이블 | 인덱스명 | 컬럼 | 목적 |
|--------|----------|------|------|
| `reports` | `idx_reports_user_id` | `user_id` | 내 제보 목록 |
| `reports` | `idx_reports_report_type` | `report_type` | LOST/FOUND 필터 |
| `reports` | `idx_reports_created_at` | `created_at` | 최신순 정렬 |
| `report_features` | `idx_report_features_report_id` | `report_id` | 제보별 태그 조회 |
| `report_features` | `idx_report_features_report_category` | `report_id`, `category` | 카테고리별 태그 조회 |
| `report_photos` | `idx_report_photos_report_id` | `report_id` | 제보별 사진 조회 |
| `report_embeddings` | `idx_report_embeddings_report_id` | `report_id` | 제보별 벡터 조회 |
| `animals` | `idx_animals_updated_at` | `updated_at` | 증분 동기화 |
| `animals` | `idx_animals_source_upd_tm` | `source_upd_tm` | 공공 원본 갱신 비교 |
| `animals` | `idx_animals_up_kind_cd` | `up_kind_cd` | 축종 필터 |
| `animals` | `idx_animals_notice_edt` | `notice_edt` | 공고 만료 필터 |
| `animal_embeddings` | `idx_animal_embeddings_desertion_no` | `desertion_no` | 공고별 갤러리 조회 |
| `animal_embeddings` | `idx_animal_embeddings_model` | `model_version`, `preprocess_version` | 모델 버전별 재적재 |
| `match_runs` | `idx_match_runs_report_id` | `report_id` | 제보별 매칭 이력 |
| `match_runs` | `idx_match_runs_report_created` | `report_id`, `created_at` | 최근 매칭 조회 |
| `match_results` | `idx_match_results_run_id` | `match_run_id` | 실행별 후보 조회 |
| `match_results` | `idx_match_results_desertion_no` | `desertion_no` | 보호소 후보 역방향 조회 |
| `match_results` | `idx_match_results_candidate_report` | `candidate_report_id` | 제보 후보 역방향 조회 |

## 4. 애플리케이션 레벨 제약

DB 제약으로 표현하지 않고 엔티티 콜백에서 검증하는 규칙이다.

`MatchResult`는 `candidate_type`에 따라 후보 식별자가 배타적이어야 한다.
`@PrePersist` / `@PreUpdate`에서 검증한다.

| `candidate_type` | `desertion_no` | `candidate_report_id` |
|------------------|----------------|----------------------|
| `SHELTER` | 필수 | 반드시 null |
| `REPORT` | 반드시 null | 필수 |

기본값도 엔티티 콜백에서 채운다.

| 테이블 | 컬럼 | 기본값 |
|--------|------|--------|
| `users` | `role` | `USER` |
| `reports` | `status` | `OPEN` |
| `match_runs` | `status` | `DONE` |
| `match_results` | `near_duplicate` | `false` |
| `animal_embeddings` | `photo_index` | `0` |
| `shelter_locations` | `geocode_status` | `PENDING` |

## 5. ER 다이어그램 (DBML)

[dbdiagram.io](https://dbdiagram.io)에 붙여넣으면 ER 다이어그램이 생성된다.

`Ref`는 논리적 관계다. 앞서 설명한 대로 DB 외래키 제약은 존재하지 않는다.

```
// PawPawFind DB Schema
// JPA 엔티티 기준. 인덱스/제약은 ddl-auto=update가 엔티티에서 생성한다.
// Ref는 논리적 관계이며 DB FK 제약은 없다(@ManyToOne 미사용).

// =========================
// Users — 소셜 로그인
// BE: User.java
// =========================

Table users {
  user_id bigint [pk, increment]
  provider varchar(20) [not null, note: 'KAKAO | GOOGLE']
  provider_id varchar(100) [not null, note: '카카오/구글 고유 ID']
  nickname varchar(50) [not null]
  role varchar(20) [not null, default: 'USER', note: 'USER | ADMIN']

  created_at timestamp [not null]
  updated_at timestamp [not null]

  indexes {
    (provider, provider_id) [unique, name: 'uk_users_provider']
  }
}


// =========================
// Animals — 공공 API 보호소 공고. PK = 공공 고유번호
// BE: Animal.java
// 입양/봉사(adptn*, srvc*) 필드는 저장하지 않는다.
// =========================

Table animals {
  desertion_no varchar(20) [pk, note: '공공 고유번호']

  happen_dt varchar(8) [note: '발견일 YYYYMMDD']
  happen_place varchar(255)

  up_kind_cd varchar(10)
  up_kind_nm varchar(20)

  kind_cd varchar(10)
  kind_nm varchar(100)
  kind_full_nm varchar(150)

  color_cd varchar(50)
  age varchar(50)
  weight varchar(50)

  notice_no varchar(50)
  notice_sdt varchar(8)
  notice_edt varchar(8)

  popfile1 text
  popfile2 text

  process_state varchar(30)

  sex_cd varchar(1) [note: 'M | F | Q']
  neuter_yn varchar(1) [note: 'Y | N | U']

  special_mark text

  care_reg_no varchar(30)
  care_nm varchar(100)
  care_tel varchar(30)
  care_addr varchar(255)

  org_nm varchar(100)
  source_upd_tm varchar(30) [note: '공공 원본 갱신시각']

  created_at timestamp
  updated_at timestamp

  indexes {
    updated_at [name: 'idx_animals_updated_at']
    source_upd_tm [name: 'idx_animals_source_upd_tm']
    up_kind_cd [name: 'idx_animals_up_kind_cd']
    notice_edt [name: 'idx_animals_notice_edt']
  }
}


// =========================
// Shelter Locations — 보호소 주소 지오코딩 결과 (카카오 Local API)
// BE: ShelterLocation.java, GeocodeStatus.java
// =========================

Table shelter_locations {
  id bigint [pk, increment]

  shelter_key varchar(80) [not null, unique, note: '보호소 식별 키']

  care_reg_no varchar(30)
  care_nm varchar(100)
  care_tel varchar(30)
  care_addr varchar(255)

  normalized_address varchar(255) [note: '정규화한 주소']
  address_hash varchar(64) [note: '주소 변경 감지용 해시']

  latitude double
  longitude double

  geocode_status varchar(20) [not null, default: 'PENDING', note: 'PENDING | SUCCESS | NOT_FOUND | FAILED']
  geocode_provider varchar(20) [note: 'KAKAO']
  attempt_count integer [not null, default: 0]
  geocoded_at timestamp

  created_at timestamp [not null]
  updated_at timestamp [not null]

  indexes {
    shelter_key [unique, name: 'uk_shelter_locations_shelter_key']
  }
}


// =========================
// Reports — 실종(LOST) / 목격(FOUND) 제보
// BE: Reports.java
// 털색은 report_features (category=털색) 로 분리
// =========================

Table reports {
  report_id bigint [pk, increment]

  user_id bigint [note: '로그인 시 설정. 익명 제보는 null']

  report_type varchar(10) [not null, note: 'LOST | FOUND']
  title varchar(255) [note: 'FOUND만 사용']

  species varchar(20) [not null, note: '강아지 | 고양이']
  size varchar(10) [not null, note: '소형 | 중형 | 대형']

  event_date date [not null, note: 'LOST=실종일, FOUND=목격일']
  event_hour integer [note: '0~23, 모르면 null']

  happen_place varchar(255) [not null]
  latitude double [not null]
  longitude double [not null]

  description text [note: 'nullable']
  status varchar(20) [not null, default: 'OPEN', note: 'OPEN | CLOSED']

  created_at timestamp [not null]
  updated_at timestamp [not null]

  indexes {
    user_id [name: 'idx_reports_user_id']
    report_type [name: 'idx_reports_report_type']
    created_at [name: 'idx_reports_created_at']
  }
}


// =========================
// Report Features — 사용자 선택 외형 태그 (AI rerank 입력)
// BE: ReportFeatures.java
// =========================

Table report_features {
  id bigint [pk, increment]

  report_id bigint [not null]

  category varchar(30) [not null, note: '털색 | 털길이 | 귀 | 꼬리 | ...']
  keyword varchar(50) [not null, note: '흰색 | 장모 | 귀 접힘 | ...']

  indexes {
    report_id [name: 'idx_report_features_report_id']
    (report_id, category) [name: 'idx_report_features_report_category']
  }
}


// =========================
// Report Photos — S3 URL만 저장. 업로드는 presigned PUT으로 브라우저가 직접 수행
// BE: ReportPhotos.java
// =========================

Table report_photos {
  id bigint [pk, increment]

  report_id bigint [not null]
  photo_url text [not null]
  sort_order integer

  created_at timestamp
  updated_at timestamp

  indexes {
    report_id [name: 'idx_report_photos_report_id']
  }
}


// =========================
// Match Runs — 제보 1건 기준 AI 매칭 1회 실행
// BE: MatchRun.java
// =========================

Table match_runs {
  id bigint [pk, increment]

  report_id bigint [not null, note: '어떤 제보로 검색했는지']

  model_version varchar(50) [not null, note: '예: avito-dinov2-small-v1.1']
  rerank_version varchar(50) [note: '예: pawpawfind-spatiotemporal-reranker-v11.0']

  decision varchar(50) [not null, note: 'NO_GALLERY | NEAR_DUPLICATE_CANDIDATE | RANKED_CANDIDATES_NEED_HUMAN_REVIEW']
  status varchar(20) [not null, default: 'DONE', note: 'PENDING | RUNNING | DONE | FAILED']

  created_at timestamp [not null]

  indexes {
    report_id [name: 'idx_match_runs_report_id']
    (report_id, created_at) [name: 'idx_match_runs_report_created']
  }
}


// =========================
// Match Results — Top-N 후보
// BE: MatchResult.java
// candidate_type에 따라 desertion_no / candidate_report_id 가 배타적 (엔티티 콜백에서 검증)
// =========================

Table match_results {
  id bigint [pk, increment]

  match_run_id bigint [not null]
  rank smallint [not null]

  candidate_type varchar(10) [not null, note: 'SHELTER | REPORT']

  desertion_no varchar(20) [note: 'SHELTER 후보만. REPORT면 null']
  candidate_report_id bigint [note: 'REPORT 후보만. SHELTER면 null']

  visual_score decimal(8,5) [not null, note: 'Re-ID 코사인 유사도']
  ranking_score decimal(8,5) [note: 'rerank 후 최종 점수 (정렬 기준)']
  tag_score decimal(8,5)
  text_score decimal(8,5)

  phash_distance smallint
  near_duplicate boolean [not null, default: false]

  matched_tags json [note: 'PostgreSQL jsonb']
  conflicting_tags json

  gallery_id varchar(100) [note: 'AI 갤러리 ID']
  image_url text [note: '표시용 대표 사진']

  created_at timestamp [not null]

  indexes {
    (match_run_id, rank) [unique, name: 'uk_match_results_run_rank']
    match_run_id [name: 'idx_match_results_run_id']
    desertion_no [name: 'idx_match_results_desertion_no']
    candidate_report_id [name: 'idx_match_results_candidate_report']
  }
}


// =========================
// Animal Embeddings — 보호소 사진 Re-ID 갤러리. AI batch 적재
// BE: AnimalEmbedding.java
// pgvector 도입 시 embedding vector(384) 추가
// =========================

Table animal_embeddings {
  id bigint [pk, increment]

  desertion_no varchar(20) [not null]
  gallery_id varchar(100) [not null, unique, note: 'species:record_id:photo_index']

  species varchar(10) [not null, note: '검색 시 1차 필터']

  photo_index smallint [not null, default: 0]
  image_url text [not null]

  phash_full varchar(32)
  phash_crop varchar(32)

  model_version varchar(50) [not null]
  preprocess_version varchar(50) [not null]

  embedding_ref text [note: 'S3/npz 경로 참조키']

  detection_confidence decimal(6,4) [note: 'YOLO 검출 신뢰도']
  blur_score decimal(8,2) [note: '흐림 정도. 저품질 사진 제외 판단']

  created_at timestamp [not null]
  updated_at timestamp [not null]

  indexes {
    desertion_no [name: 'idx_animal_embeddings_desertion_no']
    (model_version, preprocess_version) [name: 'idx_animal_embeddings_model']
  }
}


// =========================
// Report Embeddings — 제보 사진별 Re-ID 벡터 (쿼리용)
// BE: ReportEmbedding.java
// =========================

Table report_embeddings {
  id bigint [pk, increment]

  report_photo_id bigint [not null, unique]
  report_id bigint [not null, note: '제보 단위 조회용']

  model_version varchar(50) [not null]
  preprocess_version varchar(50) [not null]

  phash_full varchar(32)
  phash_crop varchar(32)

  embedding_ref text

  created_at timestamp [not null]
  updated_at timestamp [not null]

  indexes {
    report_id [name: 'idx_report_embeddings_report_id']
  }
}


// =========================
// Relationships — 논리적 관계. DB FK 제약은 없다.
// =========================

Ref: reports.user_id > users.user_id

Ref: report_features.report_id > reports.report_id
Ref: report_photos.report_id > reports.report_id

Ref: match_runs.report_id > reports.report_id
Ref: match_results.match_run_id > match_runs.id
Ref: match_results.desertion_no > animals.desertion_no
Ref: match_results.candidate_report_id > reports.report_id

Ref: animal_embeddings.desertion_no > animals.desertion_no
Ref: report_embeddings.report_photo_id > report_photos.id
Ref: report_embeddings.report_id > reports.report_id
```

`shelter_locations`는 `animals`의 보호소 필드(`care_reg_no`, `care_nm`, `care_addr`)에서
`shelter_key`를 만들어 연결한다. `animals` 쪽에 유니크 키가 없어 `Ref`로 표현하지 않았다.

## 6. 확장 계획

임베딩 벡터는 현재 DB에 저장하지 않는다. `embedding_ref`로 S3/npz 경로만 두고,
AI 서버가 벡터를 메모리에 올려 NumPy로 전수 검색한다.

데이터가 늘어 전수 검색이 한계에 닿으면 `pgvector` 확장을 도입해
`animal_embeddings.embedding vector(384)` 컬럼을 추가하는 것이 첫 후보다.
이미 PostgreSQL을 쓰고 있어 운영할 컴포넌트가 늘지 않는다.
