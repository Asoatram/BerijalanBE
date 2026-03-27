# API Contracts
## Screen: Check-in Successful (`node-id=1:160`)

## Page Description
This page confirms a completed check-in, shows the recorded check-in time and next-step instructions, and routes the visitor forward via `Continue`.

## Conventions
- Base path: `/api/v1`
- Content-Type: `application/json`
- Auth: `Bearer <kiosk_or_app_token>`

## Error Envelope
```json
{
  "error": {
    "code": "CHECKIN_NOT_FOUND",
    "message": "Check-in record was not found",
    "details": [],
    "trace_id": "6bcb7a65-57e7-4a2d-b7eb-c5f4eead6f31"
  }
}
```

## 1. Get Check-In Detail
`GET /api/v1/checkins/{checkin_id}`

Response `200`:
```json
{
  "data": {
    "checkin_id": "56a1fdd2-a9e0-43f7-a740-81d0df8cf2c8",
    "status": "CHECKED_IN",
    "checkin_at": "2026-10-24T03:45:00Z",
    "next_step": {
      "code": "GO_TO_SECURITY_DESK",
      "title": "NEXT STEP",
      "message": "Please proceed to the security desk for further assistance. A digital pass has been issued to your profile."
    },
    "digital_pass": {
      "pass_id": "1a00fe49-4d13-4d3e-9edf-83c2f3d6cc6b",
      "pass_number": "VG-2026-001245",
      "status": "ISSUED"
    }
  }
}
```

Errors:
- `401 UNAUTHORIZED`
- `404 CHECKIN_NOT_FOUND`
- `500 INTERNAL_SERVER_ERROR`

## 2. Continue from Success Screen
`POST /api/v1/checkins/{checkin_id}/continue`

Request:
```json
{
  "action": "CONTINUE_FROM_SUCCESS",
  "device_id": "kiosk-lobby-01"
}
```

Response `200`:
```json
{
  "data": {
    "next_route": "/digital-pass/1a00fe49-4d13-4d3e-9edf-83c2f3d6cc6b",
    "next_screen": "DIGITAL_PASS",
    "checkin_id": "56a1fdd2-a9e0-43f7-a740-81d0df8cf2c8"
  }
}
```

Alternative response `200`:
```json
{
  "data": {
    "next_route": "/home",
    "next_screen": "HOME",
    "checkin_id": "56a1fdd2-a9e0-43f7-a740-81d0df8cf2c8"
  }
}
```

Errors:
- `401 UNAUTHORIZED`
- `404 CHECKIN_NOT_FOUND`
- `409 CHECKIN_NOT_ELIGIBLE_FOR_CONTINUE`
- `500 INTERNAL_SERVER_ERROR`
