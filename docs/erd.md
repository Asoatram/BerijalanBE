# Vigi-Gate ERD

```mermaid
erDiagram
    USERS {
        uuid user_id PK
        string full_name
        string email UK
        string password_hash
        string account_status
        datetime last_login_at
        datetime created_at
        datetime updated_at
    }

    ROLES {
        uuid role_id PK
        string code UK
        string name
        string description
    }

    PERMISSIONS {
        uuid permission_id PK
        string code UK
        string description
    }

    USER_ROLES {
        uuid user_role_id PK
        uuid user_id FK
        uuid role_id FK
        datetime assigned_at
    }

    ROLE_PERMISSIONS {
        uuid role_permission_id PK
        uuid role_id FK
        uuid permission_id FK
        datetime assigned_at
    }

    AUTH_REFRESH_TOKENS {
        uuid refresh_token_id PK
        uuid user_id FK
        string token_hash UK
        datetime expires_at
        datetime revoked_at
        string client_id
        datetime created_at
    }

    DEVICES {
        string device_id PK
        string device_type
        string location_label
        string status
        datetime last_seen_at
    }

    VISITORS {
        uuid visitor_id PK
        string full_name
        string nik UK
        string email
        string kyc_status
        datetime created_at
        datetime updated_at
    }

    VISIT_INTENTS {
        uuid purpose_id PK
        string code UK
        string label
        int sort_order
        bool is_active
    }

    PORTRAIT_MEDIA {
        uuid photo_id PK
        string storage_url
        string mime_type
        string checksum_sha256
        decimal quality_score
        string processing_status
        datetime expires_at
        string captured_device_id FK
        datetime created_at
    }

    CHECKINS {
        uuid checkin_id PK
        uuid visitor_id FK
        uuid purpose_id FK
        uuid photo_id FK
        string device_id FK
        string status
        datetime checkin_at
        datetime created_at
    }

    VISIT_SESSIONS {
        uuid session_id PK
        uuid checkin_id FK
        uuid visitor_id FK
        uuid host_contact_id FK
        string status
        datetime checkin_at
        datetime checkout_at
        datetime created_at
        datetime updated_at
    }

    CHECKOUTS {
        uuid checkout_id PK
        uuid session_id FK
        uuid performed_by_user_id FK
        string performed_by_device_id FK
        string reason_code
        string reason_note
        bool is_forced
        string status
        datetime checkout_at
        datetime created_at
    }

    DIGITAL_PASSES {
        uuid pass_id PK
        uuid session_id FK
        string pass_number UK
        string status
        datetime issued_at
        datetime revoked_at
    }

    ACCESS_KEYS {
        uuid access_key_id PK
        uuid session_id FK
        string key_type
        string status
        datetime issued_at
        datetime revoked_at
    }

    HOST_CONTACTS {
        uuid host_contact_id PK
        string full_name
        string department
        string floor_label
        string phone
        string email
        bool is_active
    }

    HOST_NOTIFICATIONS {
        uuid notification_id PK
        uuid session_id FK
        uuid host_contact_id FK
        uuid requested_by_user_id FK
        string channel
        string message
        string status
        datetime sent_at
        datetime created_at
    }

    PRINT_JOBS {
        uuid job_id PK
        uuid session_id FK
        uuid pass_id FK
        uuid requested_by_user_id FK
        string printer_id
        int copies
        string status
        datetime queued_at
        datetime completed_at
    }

    RISK_ANALYSES {
        uuid risk_analysis_id PK
        uuid session_id FK
        string risk_level
        int risk_score
        string summary
        string model_version
        datetime computed_at
    }

    RISK_SIGNALS {
        uuid signal_id PK
        uuid risk_analysis_id FK
        string signal_type
        decimal weight
        string value_text
        datetime observed_at
    }

    RISK_ALERTS {
        uuid alert_id PK
        uuid visitor_id FK
        uuid session_id FK
        string severity
        string title
        string description
        string status
        datetime created_at
        datetime closed_at
    }

    RISK_ALERT_ACTIONS {
        uuid alert_action_id PK
        uuid alert_id FK
        uuid acted_by_user_id FK
        string action
        string note
        datetime acted_at
    }

    SESSION_FLAGS {
        uuid flag_id PK
        uuid session_id FK
        uuid created_by_user_id FK
        string flag_type
        string note
        string status
        datetime created_at
        datetime closed_at
    }

    SESSION_REPORTS {
        uuid report_id PK
        uuid session_id FK
        string format
        string storage_url
        datetime expires_at
        datetime generated_at
        uuid generated_by_user_id FK
    }

    DAILY_REPORTS {
        uuid daily_report_id PK
        date report_date
        string timezone
        string title
        string subtitle
        string author
        decimal confidence_score
        datetime last_sync_at
        datetime generated_at
        string status
    }

    DAILY_REPORT_SUMMARIES {
        uuid daily_report_id PK_FK
        int total_visitors
        decimal total_visitors_change_pct
        string peak_traffic_window
        decimal peak_window_share_pct
        int alerts_triggered
        string alerts_resolution_status
    }

    DAILY_REPORT_ANOMALIES {
        uuid anomaly_id PK
        uuid daily_report_id FK
        string severity
        string description
        int sort_order
    }

    DAILY_REPORT_RECOMMENDATIONS {
        uuid recommendation_id PK
        uuid daily_report_id FK
        string recommendation_text
        int sort_order
    }

    DAILY_REPORT_DATA_SOURCES {
        uuid data_source_id PK
        uuid daily_report_id FK
        string source_name
        int sort_order
    }

    REPORT_GENERATION_JOBS {
        uuid job_id PK
        date report_date
        string timezone
        bool force_regenerate
        string status
        
        
        uuid requested_by_user_id FK
        datetime queued_at
        datetime started_at
        datetime completed_at
        uuid daily_report_id FK
    }

    REPORT_EXPORTS {
        uuid export_id PK
        uuid daily_report_id FK
        uuid exported_by_user_id FK
        string format
        string include_sections_json
        string download_url
        datetime expires_at
        datetime created_at
    }

    USERS ||--o{ USER_ROLES : has
    ROLES ||--o{ USER_ROLES : assigned_to
    ROLES ||--o{ ROLE_PERMISSIONS : has
    PERMISSIONS ||--o{ ROLE_PERMISSIONS : grants
    USERS ||--o{ AUTH_REFRESH_TOKENS : owns

    DEVICES ||--o{ PORTRAIT_MEDIA : captures
    DEVICES ||--o{ CHECKINS : submits
    DEVICES ||--o{ CHECKOUTS : performs

    VISITORS ||--o{ CHECKINS : registers
    VISIT_INTENTS ||--o{ CHECKINS : classifies
    PORTRAIT_MEDIA ||--o{ CHECKINS : verifies

    CHECKINS ||--|| VISIT_SESSIONS : opens
    VISITORS ||--o{ VISIT_SESSIONS : has
    HOST_CONTACTS ||--o{ VISIT_SESSIONS : receives

    VISIT_SESSIONS ||--o| CHECKOUTS : ends_with
    USERS ||--o{ CHECKOUTS : executes

    VISIT_SESSIONS ||--o| DIGITAL_PASSES : issues
    VISIT_SESSIONS ||--o{ ACCESS_KEYS : grants

    VISIT_SESSIONS ||--o{ HOST_NOTIFICATIONS : triggers
    HOST_CONTACTS ||--o{ HOST_NOTIFICATIONS : targeted
    USERS ||--o{ HOST_NOTIFICATIONS : requested_by

    VISIT_SESSIONS ||--o{ PRINT_JOBS : prints
    DIGITAL_PASSES ||--o{ PRINT_JOBS : source
    USERS ||--o{ PRINT_JOBS : requested_by

    VISIT_SESSIONS ||--o| RISK_ANALYSES : scored_by
    RISK_ANALYSES ||--o{ RISK_SIGNALS : composed_of

    VISITORS ||--o{ RISK_ALERTS : associated_with
    VISIT_SESSIONS ||--o{ RISK_ALERTS : raised_for
    RISK_ALERTS ||--o{ RISK_ALERT_ACTIONS : acted_on
    USERS ||--o{ RISK_ALERT_ACTIONS : acts

    VISIT_SESSIONS ||--o{ SESSION_FLAGS : flagged_with
    USERS ||--o{ SESSION_FLAGS : flags

    VISIT_SESSIONS ||--o{ SESSION_REPORTS : reported_as
    USERS ||--o{ SESSION_REPORTS : generated_by

    DAILY_REPORTS ||--|| DAILY_REPORT_SUMMARIES : has
    DAILY_REPORTS ||--o{ DAILY_REPORT_ANOMALIES : includes
    DAILY_REPORTS ||--o{ DAILY_REPORT_RECOMMENDATIONS : includes
    DAILY_REPORTS ||--o{ DAILY_REPORT_DATA_SOURCES : sourced_from
    DAILY_REPORTS ||--o{ REPORT_EXPORTS : exported_as
    USERS ||--o{ REPORT_EXPORTS : exported_by

    USERS ||--o{ REPORT_GENERATION_JOBS : requests
    DAILY_REPORTS ||--o{ REPORT_GENERATION_JOBS : materialized_by
```

## Notes
- `VISIT_SESSIONS` is the core lifecycle record that links check-in, risk analysis, digital pass, alerts, flags, and checkout.
- `RISK_ANALYSES` and `RISK_SIGNALS` support the project requirement to assign a risk score from behavior patterns.
- `DAILY_REPORTS` + child tables support the Reports & Insights screen (summary, anomalies, recommendations, metadata, and exports).
- `AUTH_REFRESH_TOKENS`, `USER_ROLES`, and `ROLE_PERMISSIONS` support admin/security authentication and authorization flows.
