# API Contracts
## Screen: Active Visitor Session (`node-id=1:227`)

## Page Description
This page displays an active visitor session with identity, check-in time, and visit purpose, and allows security/admin to complete visitor departure via `Check Out`.

## Conventions
- Base path: `/api/v1`
- Content-Type: `application/json`
- Auth:
`GET /api/v1/visitors/{visitor_id}/active-session`: public (no token)
`POST /api/v1/checkouts`: public (no token)
`GET /api/v1/sessions/{session_id}`: `Bearer <admin_or_security_token>`
- `Idempotency-Key` header required for `POST /api/v1/checkouts`

## Error Envelope
```json
{
  "error": {
    "code": "ACTIVE_SESSION_NOT_FOUND",
    "message": "Active visitor session not found",
    "details": [],
    "trace_id": "0f3ce0b7-22b8-45f5-a4f5-06d09c0f6139"
  }
}
```

## 1. Get Active Visitor Session
`GET /api/v1/visitors/{visitor_id}/active-session`

Response `200`:
```json
{
  "data": {
    "session_id": "4c88f14f-078a-4d35-a8fa-f2f17a7a0c8b",
    "status": "ACTIVE",
    "visitor": {
      "visitor_id": "9f945962-c0ac-44c6-a9ef-c46552c63e76",
      "full_name": "Ahmad Sudirman",
      "nik": "3201234567890001"
    },
    "checkin": {
      "checkin_id": "56a1fdd2-a9e0-43f7-a740-81d0df8cf2c8",
      "checkin_at": "2026-10-24T03:45:00Z"
    },
    "visit_intent": {
      "purpose_id": "0e850d09-d206-4cbf-9143-fba4ba4f01b8",
      "label": "Scheduled Meeting - Finance Dept"
    }
  }
}
```

Errors:
- `404 ACTIVE_SESSION_NOT_FOUND`
- `500 INTERNAL_SERVER_ERROR`

## 2. Check Out Active Session
`POST /api/v1/checkouts`

Request:
```json
{
  "session_id": "4c88f14f-078a-4d35-a8fa-f2f17a7a0c8b",
  "checkout_by": "security-desk-01",
  "reason": "VISIT_COMPLETED"
}
```

Response `201`:
```json
{
  "data": {
    "checkout_id": "7601ffb3-ae58-4fd8-ab5f-66ca5be4d6f6",
    "session_id": "4c88f14f-078a-4d35-a8fa-f2f17a7a0c8b",
    "status": "CHECKED_OUT",
    "checkout_at": "2026-10-24T05:12:00Z",
    "access": {
      "temporary_keys_revoked": true,
      "revoked_at": "2026-10-24T05:12:01Z"
    }
  }
}
```

Alternative response `202` (async key revocation):
```json
{
  "data": {
    "checkout_id": "7601ffb3-ae58-4fd8-ab5f-66ca5be4d6f6",
    "session_id": "4c88f14f-078a-4d35-a8fa-f2f17a7a0c8b",
    "status": "CHECKOUT_PENDING_ACCESS_REVOCATION",
    "checkout_at": "2026-10-24T05:12:00Z"
  }
}
```

Errors:
- `400 VALIDATION_ERROR`
- `404 ACTIVE_SESSION_NOT_FOUND`
- `409 SESSION_ALREADY_CHECKED_OUT`
- `409 IDEMPOTENCY_CONFLICT`
- `500 CHECKOUT_FAILED`

## 3. Get Session Detail (Post-Checkout Refresh)
`GET /api/v1/sessions/{session_id}`

Response `200`:
```json
{
  "data": {
    "session_id": "4c88f14f-078a-4d35-a8fa-f2f17a7a0c8b",
    "status": "CHECKED_OUT",
    "checkin_at": "2026-10-24T03:45:00Z",
    "checkout_at": "2026-10-24T05:12:00Z",
    "visitor_name": "Ahmad Sudirman",
    "purpose_label": "Scheduled Meeting - Finance Dept"
  }
}
```

Errors:
- `401 UNAUTHORIZED`
- `403 FORBIDDEN`
- `404 SESSION_NOT_FOUND`
- `500 INTERNAL_SERVER_ERROR`
