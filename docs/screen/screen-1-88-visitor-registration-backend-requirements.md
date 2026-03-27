# API Contracts
## Screen: Visitor Registration (`node-id=1:88`)

## Page Description
This page collects visitor identity data, visit purpose, and a portrait photo, then submits a secure check-in request.

## Conventions
- Base path: `/api/v1`
- Content-Type:
`GET` endpoints: `application/json`
`POST /api/v1/checkins`: `multipart/form-data`
- Auth: public (no token required)
- `Idempotency-Key` header required for `POST /api/v1/checkins`

## Error Envelope
```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Request payload is invalid",
    "details": [
      { "field": "nik", "rule": "length", "message": "NIK must be exactly 16 digits" }
    ],
    "traceId": "9a12f4cb-cc52-4f4f-83e6-f1ef9082b2c7"
  }
}
```

## 1. Fetch Visit Intents
`GET /api/v1/visit-intents?active=true`

Response `200`:
```json
{
  "data": [
    { "id": "0e850d09-d206-4cbf-9143-fba4ba4f01b8", "code": "meeting", "label": "Business Meeting", "sortOrder": 1 },
    { "id": "5f3e7483-d728-4f52-9204-f2f6f6f96224", "code": "delivery", "label": "Delivery", "sortOrder": 2 }
  ]
}
```

Errors:
- `500 INTERNAL_SERVER_ERROR`

## 2. Fetch Registration Policy
`GET /api/v1/checkins/policy`

Response `200`:
```json
{
  "data": {
    "nik": { "required": true, "digits": 16 },
    "fullName": { "required": true, "minLength": 3, "maxLength": 150 },
    "photo": {
      "required": true,
      "allowedMimeTypes": ["image/jpeg", "image/png"],
      "maxSizeBytes": 5242880,
      "minQualityScore": 0.65
    }
  }
}
```

Errors:
- `500 INTERNAL_SERVER_ERROR`

## 3. Create Check-In (Single Step Multipart)
`POST /api/v1/checkins`

Request (`multipart/form-data`):
- `fullName`: `Alya Pratama`
- `nik`: `3174012345678912`
- `purposeId`: `0e850d09-d206-4cbf-9143-fba4ba4f01b8`
- `deviceId`: `kiosk-lobby-01`
- `photo`: `<binary image file>`

Example curl:
```bash
curl -X POST "http://localhost:8080/api/v1/checkins" \
  -H "Idempotency-Key: checkin-001" \
  -F "fullName=Alya Pratama" \
  -F "nik=3174012345678912" \
  -F "purposeId=0e850d09-d206-4cbf-9143-fba4ba4f01b8" \
  -F "deviceId=kiosk-lobby-01" \
  -F "photo=@/path/to/portrait.jpg;type=image/jpeg"
```

Response `201`:
```json
{
  "data": {
    "checkinId": "56a1fdd2-a9e0-43f7-a740-81d0df8cf2c8",
    "visitorId": "9f945962-c0ac-44c6-a9ef-c46552c63e76",
    "sessionId": "8e8e9202-995f-4aac-bfd2-1b20c95f8658",
    "status": "CHECKED_IN",
    "checkinAt": "2026-03-26T14:52:45Z"
  }
}
```

Alternative response `201`:
```json
{
  "data": {
    "checkinId": "56a1fdd2-a9e0-43f7-a740-81d0df8cf2c8",
    "visitorId": "9f945962-c0ac-44c6-a9ef-c46552c63e76",
    "sessionId": "8e8e9202-995f-4aac-bfd2-1b20c95f8658",
    "status": "PENDING_REVIEW",
    "message": "Check-in submitted and awaiting security approval"
  }
}
```

Errors:
- `400 VALIDATION_ERROR`
- `400 INVALID_IMAGE_FORMAT`
- `413 IMAGE_TOO_LARGE`
- `404 PURPOSE_NOT_FOUND`
- `409 ACTIVE_VISIT_EXISTS`
- `409 IDEMPOTENCY_CONFLICT`
- `503 MEDIA_PROCESSING_FAILED`
- `500 CHECKIN_CREATE_FAILED`
