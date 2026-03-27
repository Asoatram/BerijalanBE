# API Contracts
## Screen: Admin Dashboard (`node-id=1:368`)

## Page Description
This page gives admins/security a real-time operational overview: visitor KPIs, searchable/filterable visitor logs, risk alerts, and badge queue count, with quick actions for registering visitors and handling incidents.

## Conventions
- Base path: `/api/v1`
- Content-Type: `application/json`
- Auth: `Bearer <admin_or_security_token>`
- Timezone-aware queries supported via `timezone` parameter (e.g., `Asia/Jakarta`)

## Error Envelope
```json
{
  "error": {
    "code": "FORBIDDEN",
    "message": "You do not have permission to access this resource",
    "details": [],
    "trace_id": "2fbc4bd3-7cd8-4a0e-bdf9-8f9fcb7c2188"
  }
}
```

## 1. Get Dashboard KPI Summary
`GET /api/v1/dashboard/summary?date=2026-10-24&timezone=Asia/Jakarta`

Response `200`:
```json
{
  "data": {
    "total_visitors_today": 42,
    "currently_active": 12,
    "high_risk_visitors": 3,
    "badge_queue_count": 5,
    "generated_at": "2026-10-24T09:50:00+07:00"
  }
}
```

Errors:
- `401 UNAUTHORIZED`
- `403 FORBIDDEN`
- `422 INVALID_DATE_OR_TIMEZONE`
- `500 INTERNAL_SERVER_ERROR`

## 2. List Visitors (Search, Filter, Pagination)
`GET /api/v1/visitors?query=ahmad&status=ACTIVE&risk_level=ALL&timeframe=today&page=1&page_size=10&sort_by=checkin_at&sort_dir=desc`

Response `200`:
```json
{
  "data": [
    {
      "session_id": "8e8e9202-995f-4aac-bfd2-1b20c95f8658",
      "visitor_id": "9f945962-c0ac-44c6-a9ef-c46552c63e76",
      "name": "Ahmad Sudirman",
      "email": "ahmad.s@email.com",
      "nik": "3275082104920003",
      "purpose_label": "IT Infrastructure Maintenance",
      "checkin_time": "08:45",
      "status": "ACTIVE",
      "risk_level": "LOW"
    },
    {
      "visitor_id": "2fd1e4ff-f9c2-4f31-a277-a73a8ccfbc0e",
      "name": "John Doe",
      "email": "j.doe@contractor.net",
      "nik": "9982736450192837",
      "purpose_label": "Server Room Access (Unscheduled)",
      "checkin_time": "09:12",
      "status": "ACTIVE",
      "risk_level": "HIGH"
    }
  ],
  "meta": {
    "page": 1,
    "page_size": 10,
    "total_records": 42,
    "total_pages": 5
  }
}
```

Notes:
- `session_id` can be used directly with `GET /api/v1/sessions/{session_id}/detail`.
- For active visitors it is their active session; for checked-out visitors it is their latest session.

Errors:
- `400 VALIDATION_ERROR`
- `401 UNAUTHORIZED`
- `403 FORBIDDEN`
- `422 INVALID_FILTER_COMBINATION`
- `500 INTERNAL_SERVER_ERROR`

## 3. Get Active Risk Alerts
`GET /api/v1/risk-alerts?status=OPEN&limit=5`

Response `200`:
```json
{
  "data": [
    {
      "alert_id": "f5a4fdc1-08f9-4fa0-8ef8-b9d39b5ad4a6",
      "severity": "HIGH",
      "title": "Unrecognized NIK detected",
      "description": "South Wing",
      "created_at": "2026-10-24T09:48:00+07:00",
      "relative_time": "2 minutes ago",
      "status": "OPEN"
    }
  ]
}
```

Errors:
- `401 UNAUTHORIZED`
- `403 FORBIDDEN`
- `500 INTERNAL_SERVER_ERROR`

## 4. Resolve or Dismiss Risk Alert
`POST /api/v1/risk-alerts/{alert_id}/actions`

Request:
```json
{
  "action": "DISMISS",
  "note": "False positive after manual verification"
}
```

Response `200`:
```json
{
  "data": {
    "alert_id": "f5a4fdc1-08f9-4fa0-8ef8-b9d39b5ad4a6",
    "status": "DISMISSED",
    "acted_by": "security-admin-01",
    "acted_at": "2026-10-24T09:51:12+07:00"
  }
}
```

Errors:
- `400 VALIDATION_ERROR`
- `401 UNAUTHORIZED`
- `403 FORBIDDEN`
- `404 ALERT_NOT_FOUND`
- `409 ALERT_ALREADY_CLOSED`
- `500 INTERNAL_SERVER_ERROR`

## 5. Register New Visitor (Quick Action Trigger)
`POST /api/v1/checkins`

Request (`multipart/form-data` + `Idempotency-Key` header):
- `fullName`: `Siti Aminah`
- `nik`: `3174032211000001`
- `purposeId`: `0e850d09-d206-4cbf-9143-fba4ba4f01b8`
- `deviceId`: `admin-dashboard-01`
- `photo`: `<binary image file>`

Example curl:
```bash
curl -X POST "http://localhost:8080/api/v1/checkins" \
  -H "Idempotency-Key: quick-register-001" \
  -F "fullName=Siti Aminah" \
  -F "nik=3174032211000001" \
  -F "purposeId=0e850d09-d206-4cbf-9143-fba4ba4f01b8" \
  -F "deviceId=admin-dashboard-01" \
  -F "photo=@/path/to/portrait.jpg;type=image/jpeg"
```

Response `201`:
```json
{
  "data": {
    "checkin_id": "12e7787c-68d2-4c6f-a7d3-cd67f2f3c3d0",
    "visitor_id": "9f945962-c0ac-44c6-a9ef-c46552c63e76",
    "session_id": "8e8e9202-995f-4aac-bfd2-1b20c95f8658",
    "status": "CHECKED_IN",
    "checkin_at": "2026-10-24T09:53:00+07:00"
  }
}
```

Errors:
- `400 VALIDATION_ERROR`
- `400 INVALID_IMAGE_FORMAT`
- `413 IMAGE_TOO_LARGE`
- `401 UNAUTHORIZED`
- `403 FORBIDDEN`
- `409 ACTIVE_VISIT_EXISTS`
- `503 MEDIA_PROCESSING_FAILED`
- `500 CHECKIN_CREATE_FAILED`
