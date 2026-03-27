# API Contracts
## Screen: Reports & Insights (`node-id=1:777`)

## Page Description
This page provides daily security intelligence for admins, including KPI summaries, AI-generated narrative insights, notable anomalies, report metadata, and actions to generate or export reports as PDF.

## Conventions
- Base path: `/api/v1`
- Content-Type: `application/json`
- Auth: `Bearer <admin_or_security_token>`
- Report date and timezone should be explicit in requests

## Error Envelope
```json
{
  "error": {
    "code": "REPORT_NOT_AVAILABLE",
    "message": "The requested report is not available",
    "details": [],
    "trace_id": "f7d3b2e6-2120-4b1f-8ab5-5d5f4f2ce8ab"
  }
}
```

## 1. Get Daily Report Overview
`GET /api/v1/reports/daily?date=2026-10-24&timezone=Asia/Jakarta`

Response `200`:
```json
{
  "data": {
    "title": "Daily Security Intelligence",
    "subtitle": "Comprehensive insights for Thursday, Oct 24, 2026",
    "summary": {
      "total_visitors": 142,
      "total_visitors_change_pct": 12,
      "peak_traffic_window": "10 AM - 12 PM",
      "peak_window_share_pct": 42,
      "alerts_triggered": 3,
      "alerts_resolution_status": "ALL_RESOLVED"
    },
    "generated_at": "2026-10-24T14:08:00+07:00"
  }
}
```

Errors:
- `401 UNAUTHORIZED`
- `403 FORBIDDEN`
- `422 INVALID_DATE_OR_TIMEZONE`
- `500 INTERNAL_SERVER_ERROR`

## 2. Get Intelligence Narrative
`GET /api/v1/reports/daily/intelligence?date=2026-10-24&timezone=Asia/Jakarta`

Response `200`:
```json
{
  "data": {
    "daily_patterns": "Today's visitor activity showed a concentrated morning surge...",
    "notable_anomalies": [
      {
        "severity": "MEDIUM",
        "description": "Two contractor entries required manual roster confirmation at the loading dock due to delayed vendor sync."
      }
    ]
  }
}
```

Errors:
- `401 UNAUTHORIZED`
- `403 FORBIDDEN`
- `404 INTELLIGENCE_NOT_READY`
- `500 INTERNAL_SERVER_ERROR`

## 3. Get Report Metadata
`GET /api/v1/reports/daily/metadata?date=2026-10-24&timezone=Asia/Jakarta`

Response `200`:
```json
{
  "data": {
    "author": "Sentinal Core AI v4.2",
    "data_sources": ["Gate A", "Gate B", "Gate C", "Gate D"],
    "confidence_score": 98.4,
    "last_sync_at": "2026-10-24T14:06:00+07:00",
    "active_zone_density_map_url": "https://cdn.vigi-gate.com/maps/density/2026-10-24.png"
  }
}
```

Errors:
- `401 UNAUTHORIZED`
- `403 FORBIDDEN`
- `404 REPORT_METADATA_NOT_FOUND`
- `500 INTERNAL_SERVER_ERROR`

## 4. Generate Report
`POST /api/v1/reports/daily/generate`

Request:
```json
{
  "date": "2026-10-24",
  "timezone": "Asia/Jakarta",
  "force_regenerate": false
}
```

Response `202`:
```json
{
  "data": {
    "job_id": "3bd87d9e-4e7c-4ca9-a311-dfdfc41fe232",
    "status": "QUEUED",
    "queued_at": "2026-10-24T14:09:12+07:00"
  }
}
```

Alternative response `200` (already ready):
```json
{
  "data": {
    "status": "READY",
    "report_id": "e8f80c0c-5ca2-4748-b0dd-8d4cc0ad5f2d",
    "generated_at": "2026-10-24T14:08:00+07:00"
  }
}
```

Errors:
- `400 VALIDATION_ERROR`
- `401 UNAUTHORIZED`
- `403 FORBIDDEN`
- `409 REPORT_GENERATION_IN_PROGRESS`
- `500 REPORT_GENERATION_FAILED`

## 5. Export Report as PDF
`POST /api/v1/reports/daily/export-pdf`

Request:
```json
{
  "date": "2026-10-24",
  "timezone": "Asia/Jakarta",
  "include_sections": ["summary", "intelligence", "metadata"]
}
```

Response `200`:
```json
{
  "data": {
    "report_id": "e8f80c0c-5ca2-4748-b0dd-8d4cc0ad5f2d",
    "format": "pdf",
    "download_url": "https://cdn.vigi-gate.com/reports/e8f80c0c-5ca2-4748-b0dd-8d4cc0ad5f2d.pdf",
    "expires_at": "2026-10-24T16:10:00+07:00"
  }
}
```

Errors:
- `400 VALIDATION_ERROR`
- `401 UNAUTHORIZED`
- `403 FORBIDDEN`
- `404 REPORT_NOT_AVAILABLE`
- `422 UNSUPPORTED_EXPORT_FORMAT`
- `500 EXPORT_FAILED`
