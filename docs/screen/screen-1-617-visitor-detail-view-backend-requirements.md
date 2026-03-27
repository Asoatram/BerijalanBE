# API Contracts
## Screen: Visitor Detail View (`node-id=1:617`)

## Page Description
This page provides a full operational profile for one active visitor session, including identity, visit purpose, duration, risk analysis, and admin actions such as printing a digital pass, contacting host, force checkout, downloading report, and flagging the session.

## Conventions
- Base path: `/api/v1`
- Content-Type: `application/json`
- Auth: `Bearer <admin_or_security_token>`
- `Idempotency-Key` header required for force-checkout and session-flagging actions

## Error Envelope
```json
{
  "error": {
    "code": "SESSION_NOT_FOUND",
    "message": "Visitor session not found",
    "details": [],
    "trace_id": "3c379215-8f34-4aa6-95c6-30c4db1184d8"
  }
}
```

## 1. Get Visitor Session Detail
`GET /api/v1/sessions/{session_id}/detail`

Response `200`:
```json
{
  "data": {
    "session_id": "4c88f14f-078a-4d35-a8fa-f2f17a7a0c8b",
    "status": "ACTIVE",
    "visitor": {
      "visitor_id": "9f945962-c0ac-44c6-a9ef-c46552c63e76",
      "full_name": "Ahmad Sudirman",
      "avatar_url": "https://cdn.vigi-gate.com/avatars/ahmad.png",
      "identity_label": "Verified Guest",
      "nik": "3201234567890001",
      "kyc_status": "COMPLIANT"
    },
    "visit": {
      "purpose_label": "Scheduled Meeting",
      "purpose_subtitle": "Finance Dept - 4th Floor",
      "checkin_at": "2026-10-24T10:45:00+07:00",
      "duration_minutes": 134
    },
    "risk_analysis": {
      "risk_level": "LOW",
      "risk_score": 5,
      "summary": "Regular visitor, matches pre-registered NIK and facial profile. No anomalies detected."
    },
    "actions": {
      "can_print_pass": true,
      "can_contact_host": true,
      "can_force_checkout": true,
      "can_flag_session": true,
      "can_download_report": true
    }
  }
}
```

Errors:
- `401 UNAUTHORIZED`
- `403 FORBIDDEN`
- `404 SESSION_NOT_FOUND`
- `500 INTERNAL_SERVER_ERROR`

## 2. Print Digital Pass
`POST /api/v1/sessions/{session_id}/print-pass`

Request:
```json
{
  "printer_id": "lobby-printer-01",
  "copies": 1
}
```

Response `202`:
```json
{
  "data": {
    "job_id": "3f8d24f4-6b4c-4d32-a890-c8de6d7d2291",
    "status": "QUEUED",
    "queued_at": "2026-10-24T13:02:11+07:00"
  }
}
```

Errors:
- `400 VALIDATION_ERROR`
- `401 UNAUTHORIZED`
- `403 FORBIDDEN`
- `404 SESSION_NOT_FOUND`
- `409 PASS_NOT_AVAILABLE`
- `503 PRINTER_UNAVAILABLE`

## 3. Contact Host
`POST /api/v1/sessions/{session_id}/contact-host`

Request:
```json
{
  "channel": "IN_APP",
  "message": "Your guest Ahmad Sudirman has arrived at the security desk."
}
```

Response `200`:
```json
{
  "data": {
    "notification_id": "33dc4de5-f810-48ca-9e42-d6f190f4b62d",
    "status": "SENT",
    "sent_at": "2026-10-24T13:03:05+07:00"
  }
}
```

Errors:
- `400 VALIDATION_ERROR`
- `401 UNAUTHORIZED`
- `403 FORBIDDEN`
- `404 HOST_CONTACT_NOT_FOUND`
- `503 NOTIFICATION_SERVICE_UNAVAILABLE`

## 4. Force Check Out (Emergency)
`POST /api/v1/sessions/{session_id}/force-checkout`

Request:
```json
{
  "reason_code": "EMERGENCY_PROTOCOL",
  "reason_note": "Immediate administrative termination of current access session."
}
```

Response `201`:
```json
{
  "data": {
    "checkout_id": "7601ffb3-ae58-4fd8-ab5f-66ca5be4d6f6",
    "session_id": "4c88f14f-078a-4d35-a8fa-f2f17a7a0c8b",
    "status": "FORCE_CHECKED_OUT",
    "checkout_at": "2026-10-24T13:05:00+07:00",
    "access_revoked": true
  }
}
```

Errors:
- `400 VALIDATION_ERROR`
- `401 UNAUTHORIZED`
- `403 FORBIDDEN`
- `404 SESSION_NOT_FOUND`
- `409 SESSION_ALREADY_CHECKED_OUT`
- `409 IDEMPOTENCY_CONFLICT`
- `500 FORCE_CHECKOUT_FAILED`

## 5. Download Visitor Session Report
`GET /api/v1/sessions/{session_id}/report?format=pdf`

Response `200`:
```json
{
  "data": {
    "report_id": "f3fe57d6-6a14-4665-b9a6-f412e2f7e3f9",
    "format": "pdf",
    "download_url": "https://cdn.vigi-gate.com/reports/f3fe57d6-6a14-4665-b9a6-f412e2f7e3f9.pdf",
    "expires_at": "2026-10-24T14:10:00+07:00"
  }
}
```

Errors:
- `401 UNAUTHORIZED`
- `403 FORBIDDEN`
- `404 SESSION_NOT_FOUND`
- `422 UNSUPPORTED_REPORT_FORMAT`
- `500 REPORT_GENERATION_FAILED`

## 6. Flag Session for Follow-Up
`POST /api/v1/sessions/{session_id}/flags`

Request:
```json
{
  "flag_type": "SECURITY_REVIEW",
  "note": "Manual follow-up requested by gate station A"
}
```

Response `201`:
```json
{
  "data": {
    "flag_id": "35f7ff5d-1f65-4a10-b92c-26d9de0a9a4d",
    "session_id": "4c88f14f-078a-4d35-a8fa-f2f17a7a0c8b",
    "status": "OPEN",
    "created_at": "2026-10-24T13:06:30+07:00"
  }
}
```

Errors:
- `400 VALIDATION_ERROR`
- `401 UNAUTHORIZED`
- `403 FORBIDDEN`
- `404 SESSION_NOT_FOUND`
- `409 FLAG_ALREADY_EXISTS`
- `409 IDEMPOTENCY_CONFLICT`
- `500 INTERNAL_SERVER_ERROR`
