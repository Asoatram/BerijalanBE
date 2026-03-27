# Risk Engine Rules

## Overview
The risk engine is a deterministic scoring system implemented in `RiskEngineService`.
It computes a `riskScore` (0-100), maps that score to `LOW`/`MEDIUM`/`HIGH`, stores per-signal evidence, and may open a high-risk alert.

## Trigger Points
Risk evaluation runs on:
- Visitor check-in (`CHECK_IN`)
- Forced checkout (`FORCE_CHECKOUT`)
- Session flagging (`SESSION_FLAGGED`)

## Score Calculation
Final score is:
- Sum of all signal weights
- Clamped to `0..100`

Formula:
- `riskScore = clamp(sum(signalWeights), 0, 100)`

## Risk Level Mapping
Using configured thresholds:
- `LOW`: `score <= lowMax` (default `34`)
- `MEDIUM`: `lowMax < score <= mediumMax` (default `69`)
- `HIGH`: `score > mediumMax`

## Signals and Weights

### 1) KYC Status
- `REVIEW_REQUIRED` -> `+25` (`weightKycReviewRequired`)
- `BLOCKED` -> `+40` (`weightKycBlocked`)
- otherwise `COMPLIANT` -> `+0`

### 2) Photo Quality (`checkIn.photo.qualityScore`)
- `>= 0.85` -> `+0`
- `>= 0.75 and < 0.85` -> `+10` (`weightPhotoMedium`)
- `>= 0.65 and < 0.75` -> `+20` (`weightPhotoLow`)
- `< 0.65` or missing -> `+35` (`weightPhotoVeryLow`, missing currently uses `weightPhotoLow` in code path)

### 3) Visit Purpose (from purpose code)
- contains `MEETING` or `SCHEDULED` -> `+5` (`weightPurposeMeeting`)
- contains `DELIVERY` -> `+15` (`weightPurposeDelivery`)
- contains `MAINTENANCE` / `VENDOR` / `CONTRACTOR` / `UNSCHEDULED` -> `+25` (`weightPurposeMaintenance`)
- other/unknown -> `+10` (`weightPurposeOther`)

### 4) Time-of-Day
Timezone: `risk.engine.timezone` (default `Asia/Jakarta`)
- business hours: `07:00 <= hour < 20:00` -> `+0`
- off-hours -> `+15` (`weightOffHours`)

### 5) Repeated Check-ins
- check-ins in last 24h (`count > 1`) -> `+20` (`weightRepeatDaily`)
- check-ins in last 7d (`count > 5`) -> `+15` (`weightRepeatWeekly`)

### 6) Open Alert History
- visitor has open HIGH alert -> `+30` (`weightOpenHighAlert`)
- else visitor has open MEDIUM alert -> `+15` (`weightOpenMediumAlert`)
- else -> `+0`

## Persistence Behavior
For each evaluation:
- Upserts one `risk_analyses` row per session
- Deletes old `risk_signals` for that analysis
- Inserts fresh `risk_signals` with:
  - `signalType`
  - `weight`
  - `valueText`
  - `observedAt`

## Summary Generation
`riskAnalysis.summary` generation order:
1. Try OpenAI summary generation (`SummaryGenerator.generateRiskSummary`)
2. On failure/blank, fallback to rule-based sentence:
   - `"Primary risk drivers: ..."` from top weighted signals

## Alert Creation Rule
If computed level is `HIGH`:
- Create `risk_alerts` entry with severity `HIGH`, status `OPEN`
- Only if no existing open high alert for the same session

Alert title and description:
- title: `High risk visitor session detected`
- description: `Score <riskScore> from <trigger>`

## Config Keys
Configured under `risk.engine.*`:
- `timezone`
- `model-version`
- `low-max`
- `medium-max`
- `business-hour-start`
- `business-hour-end`
- all `weight-*` and photo threshold keys from `RiskEngineProperties`

