package com.example.berijalanassesment.service;

import com.example.berijalanassesment.models.CheckIn;
import com.example.berijalanassesment.models.RiskAlert;
import com.example.berijalanassesment.models.RiskAnalysis;
import com.example.berijalanassesment.models.RiskSignal;
import com.example.berijalanassesment.models.VisitSession;
import com.example.berijalanassesment.repository.CheckInRepository;
import com.example.berijalanassesment.repository.RiskAlertRepository;
import com.example.berijalanassesment.repository.RiskAnalysisRepository;
import com.example.berijalanassesment.repository.RiskSignalRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RiskEngineService {

    private static final Logger log = LoggerFactory.getLogger(RiskEngineService.class);

    private final RiskEngineProperties properties;
    private final RiskAnalysisRepository riskAnalysisRepository;
    private final RiskSignalRepository riskSignalRepository;
    private final RiskAlertRepository riskAlertRepository;
    private final CheckInRepository checkInRepository;
    private final SummaryGenerator summaryGenerator;

    public RiskEngineService(
        RiskEngineProperties properties,
        RiskAnalysisRepository riskAnalysisRepository,
        RiskSignalRepository riskSignalRepository,
        RiskAlertRepository riskAlertRepository,
        CheckInRepository checkInRepository,
        SummaryGenerator summaryGenerator
    ) {
        this.properties = properties;
        this.riskAnalysisRepository = riskAnalysisRepository;
        this.riskSignalRepository = riskSignalRepository;
        this.riskAlertRepository = riskAlertRepository;
        this.checkInRepository = checkInRepository;
        this.summaryGenerator = summaryGenerator;
    }

    public RiskAnalysis evaluateAndPersist(VisitSession session, String trigger, Instant observedAt) {
        RiskComputation computation = compute(session, observedAt);

        RiskAnalysis analysis = riskAnalysisRepository.findBySessionSessionId(session.getSessionId())
            .orElseGet(RiskAnalysis::new);
        analysis.setSession(session);
        analysis.setRiskScore(computation.score());
        analysis.setRiskLevel(computation.level());
        analysis.setSummary(generateRiskSummaryWithFallback(computation));
        analysis.setModelVersion(properties.getModelVersion());
        analysis.setComputedAt(observedAt);
        RiskAnalysis savedAnalysis = riskAnalysisRepository.save(analysis);

        riskSignalRepository.deleteByRiskAnalysisRiskAnalysisId(savedAnalysis.getRiskAnalysisId());

        List<RiskSignal> signals = computation.signals().stream()
            .map(signal -> RiskSignal.builder()
                .riskAnalysis(savedAnalysis)
                .signalType(signal.signalType())
                .weight(BigDecimal.valueOf(signal.weight()))
                .valueText(signal.valueText())
                .observedAt(observedAt)
                .build())
            .toList();
        if (!signals.isEmpty()) {
            riskSignalRepository.saveAll(signals);
        }

        if ("HIGH".equals(savedAnalysis.getRiskLevel())) {
            maybeCreateHighRiskAlert(session, savedAnalysis, trigger, observedAt);
        }

        return savedAnalysis;
    }

    private RiskComputation compute(VisitSession session, Instant observedAt) {
        List<SignalPoint> signals = new ArrayList<>();
        CheckIn checkIn = session.getCheckIn();

        String kycStatus = normalize(session.getVisitor().getKycStatus());
        if ("REVIEW_REQUIRED".equals(kycStatus)) {
            signals.add(new SignalPoint("KYC_REVIEW_REQUIRED", properties.getWeightKycReviewRequired(), "kyc_status=REVIEW_REQUIRED"));
        } else if ("BLOCKED".equals(kycStatus)) {
            signals.add(new SignalPoint("KYC_BLOCKED", properties.getWeightKycBlocked(), "kyc_status=BLOCKED"));
        } else {
            signals.add(new SignalPoint("KYC_COMPLIANT", 0, "kyc_status=COMPLIANT"));
        }

        BigDecimal qualityScore = checkIn.getPhoto() == null ? null : checkIn.getPhoto().getQualityScore();
        int photoWeight = computePhotoWeight(qualityScore);
        signals.add(new SignalPoint("PHOTO_QUALITY", photoWeight, "quality_score=" + (qualityScore == null ? "null" : qualityScore.toPlainString())));

        int purposeWeight = computePurposeWeight(checkIn);
        String purposeCode = checkIn.getPurpose() == null ? "unknown" : normalize(checkIn.getPurpose().getCode()).toLowerCase(Locale.ROOT);
        signals.add(new SignalPoint("VISIT_PURPOSE", purposeWeight, "purpose=" + purposeCode));

        LocalDateTime localCheckinTime = LocalDateTime.ofInstant(
            checkIn.getCheckinAt() == null ? observedAt : checkIn.getCheckinAt(),
            ZoneId.of(properties.getTimezone())
        );
        boolean isOffHours = localCheckinTime.getHour() < properties.getBusinessHourStart()
            || localCheckinTime.getHour() >= properties.getBusinessHourEnd();
        if (isOffHours) {
            signals.add(new SignalPoint("OFF_HOURS_CHECKIN", properties.getWeightOffHours(), "checkin_hour=" + localCheckinTime.getHour()));
        } else {
            signals.add(new SignalPoint("BUSINESS_HOURS_CHECKIN", 0, "checkin_hour=" + localCheckinTime.getHour()));
        }

        Instant oneDayAgo = observedAt.minusSeconds(24 * 60 * 60);
        long checkinsPastDay = checkInRepository.countByVisitorVisitorIdAndCheckinAtBetween(
            session.getVisitor().getVisitorId(),
            oneDayAgo,
            observedAt
        );
        if (checkinsPastDay > 1) {
            signals.add(new SignalPoint("FREQUENT_CHECKIN_24H", properties.getWeightRepeatDaily(), "count_24h=" + checkinsPastDay));
        }

        Instant oneWeekAgo = observedAt.minusSeconds(7L * 24 * 60 * 60);
        long checkinsPastWeek = checkInRepository.countByVisitorVisitorIdAndCheckinAtBetween(
            session.getVisitor().getVisitorId(),
            oneWeekAgo,
            observedAt
        );
        if (checkinsPastWeek > 5) {
            signals.add(new SignalPoint("FREQUENT_CHECKIN_7D", properties.getWeightRepeatWeekly(), "count_7d=" + checkinsPastWeek));
        }

        boolean hasOpenHigh = riskAlertRepository.existsByVisitorVisitorIdAndStatusAndSeverity(
            session.getVisitor().getVisitorId(),
            "OPEN",
            "HIGH"
        );
        boolean hasOpenMedium = riskAlertRepository.existsByVisitorVisitorIdAndStatusAndSeverity(
            session.getVisitor().getVisitorId(),
            "OPEN",
            "MEDIUM"
        );
        if (hasOpenHigh) {
            signals.add(new SignalPoint("OPEN_HIGH_ALERT_HISTORY", properties.getWeightOpenHighAlert(), "open_high_alert=true"));
        } else if (hasOpenMedium) {
            signals.add(new SignalPoint("OPEN_MEDIUM_ALERT_HISTORY", properties.getWeightOpenMediumAlert(), "open_medium_alert=true"));
        } else {
            signals.add(new SignalPoint("NO_OPEN_ALERT_HISTORY", 0, "open_alerts=false"));
        }

        int score = signals.stream().mapToInt(SignalPoint::weight).sum();
        score = Math.max(0, Math.min(100, score));

        String level = toRiskLevel(score);
        return new RiskComputation(score, level, signals);
    }

    private int computePhotoWeight(BigDecimal qualityScore) {
        if (qualityScore == null) {
            return properties.getWeightPhotoLow();
        }
        if (qualityScore.compareTo(properties.getPhotoHighQualityMin()) >= 0) {
            return 0;
        }
        if (qualityScore.compareTo(properties.getPhotoMediumQualityMin()) >= 0) {
            return properties.getWeightPhotoMedium();
        }
        if (qualityScore.compareTo(properties.getPhotoLowQualityMin()) >= 0) {
            return properties.getWeightPhotoLow();
        }
        return properties.getWeightPhotoVeryLow();
    }

    private int computePurposeWeight(CheckIn checkIn) {
        if (checkIn.getPurpose() == null || checkIn.getPurpose().getCode() == null) {
            return properties.getWeightPurposeOther();
        }

        String code = normalize(checkIn.getPurpose().getCode());
        if (code.contains("MEETING") || code.contains("SCHEDULED")) {
            return properties.getWeightPurposeMeeting();
        }
        if (code.contains("DELIVERY")) {
            return properties.getWeightPurposeDelivery();
        }
        if (code.contains("MAINTENANCE") || code.contains("VENDOR") || code.contains("CONTRACTOR") || code.contains("UNSCHEDULED")) {
            return properties.getWeightPurposeMaintenance();
        }
        return properties.getWeightPurposeOther();
    }

    private String toRiskLevel(int score) {
        if (score <= properties.getLowMax()) {
            return "LOW";
        }
        if (score <= properties.getMediumMax()) {
            return "MEDIUM";
        }
        return "HIGH";
    }

    private String buildSummary(List<SignalPoint> signals) {
        List<SignalPoint> topSignals = signals.stream()
            .filter(signal -> signal.weight() > 0)
            .sorted(Comparator.comparingInt(SignalPoint::weight).reversed())
            .limit(3)
            .toList();

        if (topSignals.isEmpty()) {
            return "No elevated risk indicators detected.";
        }

        return "Primary risk drivers: " + topSignals.stream()
            .map(SignalPoint::signalType)
            .map(type -> type.toLowerCase(Locale.ROOT).replace('_', ' '))
            .reduce((left, right) -> left + ", " + right)
            .orElse("none");
    }

    private String generateRiskSummaryWithFallback(RiskComputation computation) {
        List<RiskSummarySignal> summarySignals = computation.signals().stream()
            .map(signal -> new RiskSummarySignal(signal.signalType(), signal.weight(), signal.valueText()))
            .toList();

        try {
            String generated = summaryGenerator.generateRiskSummary(
                new RiskSummaryInput(
                    computation.score(),
                    computation.level(),
                    summarySignals
                )
            );
            if (generated != null && !generated.isBlank()) {
                return generated;
            }
        } catch (RuntimeException ex) {
            log.warn("OpenAI risk summary generation failed, using fallback: {}", ex.getMessage());
        }

        return buildSummary(computation.signals());
    }

    private void maybeCreateHighRiskAlert(VisitSession session, RiskAnalysis analysis, String trigger, Instant observedAt) {
        boolean alreadyOpenHigh = riskAlertRepository.existsBySessionSessionIdAndStatusAndSeverity(
            session.getSessionId(),
            "OPEN",
            "HIGH"
        );
        if (alreadyOpenHigh) {
            return;
        }

        riskAlertRepository.save(
            RiskAlert.builder()
                .visitor(session.getVisitor())
                .session(session)
                .severity("HIGH")
                .title("High risk visitor session detected")
                .description("Score " + analysis.getRiskScore() + " from " + trigger)
                .status("OPEN")
                .createdAt(observedAt)
                .closedAt(null)
                .build()
        );
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replace('-', '_').replace(' ', '_').toUpperCase(Locale.ROOT);
    }

    private record SignalPoint(String signalType, int weight, String valueText) {
    }

    private record RiskComputation(int score, String level, List<SignalPoint> signals) {
    }
}
