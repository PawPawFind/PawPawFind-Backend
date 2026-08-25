# 주변 제보 검색 API

| 항목 | 내용 |
|------|------|
| 담당 | **김가윤** (지도 · 지도 주변 제보) |
| 목차 | [BACKEND.md](../BACKEND.md) |

## 호출

`GET /api/reports/nearby`

별도 인증 없이 지도 중심 또는 사용자 위치 주변의 공개 제보를 조회한다. 프론트엔드는 카카오맵의
현재 중심이나 사용자 위치를 `latitude`, `longitude`로 전달한다.

| 파라미터 | 필수 | 기본값 | 설명 |
|---|---:|---:|---|
| latitude | 예 | - | 기준 위도, -90 이상 90 이하의 유한한 숫자 |
| longitude | 예 | - | 기준 경도, -180 이상 180 이하의 유한한 숫자 |
| radiusMeters | 아니요 | 3000 | 검색 반경(미터), 0 초과 20000 이하 |
| reportType | 아니요 | - | `LOST` 또는 `FOUND`, 대소문자 무관 |
| species | 아니요 | - | 동물종 문자열 |
| page | 아니요 | 0 | 0 이상의 페이지 번호 |
| size | 아니요 | 20 | 페이지 크기, 1 이상 100 이하 |

문자열 필터의 앞뒤 공백은 제거한다. 검증 범위를 벗어나거나 유효하지 않은 값은 HTTP 400을
반환한다. 검색 결과는 상태가 `OPEN`인 제보로 제한하며 가까운 거리, 동일 거리일 때 reportId
오름차순으로 정렬한다. 결과가 없으면 HTTP 200과 빈 `content`를 반환한다.

## 응답 예시

```json
{
  "content": [
    {
      "reportId": 12,
      "reportType": "FOUND",
      "title": "갈색 강아지를 목격했어요",
      "species": "강아지",
      "size": "소형",
      "eventDate": "2026-08-24",
      "eventHour": 15,
      "happenPlace": "서울시 송파구",
      "latitude": 37.5001,
      "longitude": 127.1001,
      "status": "OPEN",
      "thumbnailUrl": "https://example.com/photo.jpg",
      "distanceMeters": 324
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

`thumbnailUrl`은 기존 제보 목록과 동일하게 `sortOrder`가 가장 작은 사진이며, 동률이면 사진 ID가
작은 항목이다. 사진이 없으면 `null`이다. `distanceMeters`는 Haversine 공식으로 계산한 두 좌표의
구면상 직선거리를 미터 단위 정수로 반올림한 값이다. 자동차 또는 도보 경로 거리가 아니다.

## V1 위치 검색 방식

운영 DB와 테스트용 H2에서 같은 방식으로 동작하도록 PostGIS 같은 DB 전용 공간 함수는 사용하지
않는다. 먼저 위도에 따른 경도 길이 차이를 반영한 bounding box로 DB 후보를 제한하고, Java에서
Haversine 거리를 계산해 원형 반경 밖 후보를 제거한다. 날짜변경선과 극점도 별도로 처리한다.
후보 범위를 제한하기 위해 최대 검색 반경은 20km다.
