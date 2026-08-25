# Health API

| 항목 | 내용 |
|------|------|
| 담당 | **강유진** |
| 목차 | [BACKEND.md](../BACKEND.md) |

## 호출

`GET /api/health`

인증 없음. 백엔드 프로세스 생존 확인용.

## 응답

```json
{
  "status": "ok",
  "service": "backend"
}
```
