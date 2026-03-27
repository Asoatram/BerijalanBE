# API Contracts
## Screen: Admin Access Login (`node-id=1:294`)

## Page Description
This page authenticates admin/security users using email and password, then creates a secure session for access to the Vigi-Gate management portal.

## Conventions
- Base path: `/api/v1`
- Content-Type: `application/json`
- Auth: Public for login endpoint, bearer token for post-login profile checks
- `X-Client-Id` header recommended for device/app identification

## Error Envelope
```json
{
  "error": {
    "code": "INVALID_CREDENTIALS",
    "message": "Email or password is incorrect",
    "details": [],
    "trace_id": "f2d2c2bc-879c-4bc0-9758-5e6e8b6c65f8"
  }
}
```

## 1. Admin Sign In
`POST /api/v1/auth/admin/login`

Request:
```json
{
  "email": "admin@vigi-gate.com",
  "password": "<plain-text-password>",
  "remember_me": false
}
```

Response `200`:
```json
{
  "data": {
    "access_token": "<jwt-access-token>",
    "refresh_token": "<opaque-or-jwt-refresh-token>",
    "token_type": "Bearer",
    "expires_in": 3600,
    "user": {
      "user_id": "92441c36-5ae0-4f16-9f67-8a9535d30a75",
      "name": "Security Admin",
      "email": "admin@vigi-gate.com",
      "role": "ADMIN"
    }
  }
}
```

Alternative response `200` (MFA required):
```json
{
  "data": {
    "mfa_required": true,
    "mfa_token": "<temporary-mfa-token>",
    "methods": ["TOTP", "EMAIL_OTP"]
  }
}
```

Errors:
- `400 VALIDATION_ERROR`
- `401 INVALID_CREDENTIALS`
- `403 ACCOUNT_LOCKED`
- `403 ACCOUNT_DISABLED`
- `429 TOO_MANY_ATTEMPTS`
- `500 AUTH_SERVICE_UNAVAILABLE`

## 2. Verify Session / Current User (Post-Login Bootstrap)
`GET /api/v1/auth/me`

Headers:
- `Authorization: Bearer <access_token>`

Response `200`:
```json
{
  "data": {
    "user_id": "92441c36-5ae0-4f16-9f67-8a9535d30a75",
    "name": "Security Admin",
    "email": "admin@vigi-gate.com",
    "role": "ADMIN",
    "permissions": ["VISITOR_READ", "VISITOR_CHECKOUT", "REPORT_READ"]
  }
}
```

Errors:
- `401 TOKEN_INVALID_OR_EXPIRED`
- `403 INSUFFICIENT_ROLE`
- `500 INTERNAL_SERVER_ERROR`

## 3. Refresh Access Token
`POST /api/v1/auth/refresh`

Request:
```json
{
  "refresh_token": "<opaque-or-jwt-refresh-token>"
}
```

Response `200`:
```json
{
  "data": {
    "access_token": "<new-jwt-access-token>",
    "token_type": "Bearer",
    "expires_in": 3600
  }
}
```

Errors:
- `400 VALIDATION_ERROR`
- `401 REFRESH_TOKEN_INVALID`
- `401 REFRESH_TOKEN_EXPIRED`
- `500 INTERNAL_SERVER_ERROR`
