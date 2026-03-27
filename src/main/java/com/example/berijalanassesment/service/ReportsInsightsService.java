package com.example.berijalanassesment.service;

import com.example.berijalanassesment.controller.support.ApiException;
import com.example.berijalanassesment.dto.screen.ReportsInsightsDtos;
import com.example.berijalanassesment.models.DailyReport;
import com.example.berijalanassesment.models.DailyReportAnomaly;
import com.example.berijalanassesment.models.DailyReportDataSource;
import com.example.berijalanassesment.models.DailyReportRecommendation;
import com.example.berijalanassesment.models.DailyReportSummary;
import com.example.berijalanassesment.models.ReportExport;
import com.example.berijalanassesment.models.ReportGenerationJob;
import com.example.berijalanassesment.models.User;
import com.example.berijalanassesment.models.CheckIn;
import com.example.berijalanassesment.models.RiskAlert;
import com.example.berijalanassesment.repository.CheckInRepository;
import com.example.berijalanassesment.repository.DailyReportAnomalyRepository;
import com.example.berijalanassesment.repository.DailyReportDataSourceRepository;
import com.example.berijalanassesment.repository.DailyReportRecommendationRepository;
import com.example.berijalanassesment.repository.DailyReportRepository;
import com.example.berijalanassesment.repository.DailyReportSummaryRepository;
import com.example.berijalanassesment.repository.ReportExportRepository;
import com.example.berijalanassesment.repository.ReportGenerationJobRepository;
import com.example.berijalanassesment.repository.RiskAlertRepository;
import com.example.berijalanassesment.repository.UserRepository;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class ReportsInsightsService {

    private static final Logger log = LoggerFactory.getLogger(ReportsInsightsService.class);
    private static final DateTimeFormatter HOUR_LABEL_FORMAT = DateTimeFormatter.ofPattern("h a", Locale.ENGLISH);

    private final CheckInRepository checkInRepository;
    private final RiskAlertRepository riskAlertRepository;
    private final DailyReportRepository dailyReportRepository;
    private final DailyReportSummaryRepository dailyReportSummaryRepository;
    private final DailyReportAnomalyRepository dailyReportAnomalyRepository;
    private final DailyReportRecommendationRepository dailyReportRecommendationRepository;
    private final DailyReportDataSourceRepository dailyReportDataSourceRepository;
    private final ReportGenerationJobRepository reportGenerationJobRepository;
    private final ReportExportRepository reportExportRepository;
    private final UserRepository userRepository;
    private final SummaryGenerator summaryGenerator;
    private final String apiBaseUrl;

    public ReportsInsightsService(
        CheckInRepository checkInRepository,
        RiskAlertRepository riskAlertRepository,
        DailyReportRepository dailyReportRepository,
        DailyReportSummaryRepository dailyReportSummaryRepository,
        DailyReportAnomalyRepository dailyReportAnomalyRepository,
        DailyReportRecommendationRepository dailyReportRecommendationRepository,
        DailyReportDataSourceRepository dailyReportDataSourceRepository,
        ReportGenerationJobRepository reportGenerationJobRepository,
        ReportExportRepository reportExportRepository,
        UserRepository userRepository,
        SummaryGenerator summaryGenerator,
        @Value("${app.api-base-url:http://localhost:8080}") String apiBaseUrl
    ) {
        this.checkInRepository = checkInRepository;
        this.riskAlertRepository = riskAlertRepository;
        this.dailyReportRepository = dailyReportRepository;
        this.dailyReportSummaryRepository = dailyReportSummaryRepository;
        this.dailyReportAnomalyRepository = dailyReportAnomalyRepository;
        this.dailyReportRecommendationRepository = dailyReportRecommendationRepository;
        this.dailyReportDataSourceRepository = dailyReportDataSourceRepository;
        this.reportGenerationJobRepository = reportGenerationJobRepository;
        this.reportExportRepository = reportExportRepository;
        this.userRepository = userRepository;
        this.summaryGenerator = summaryGenerator;
        this.apiBaseUrl = apiBaseUrl;
    }

    public ReportsInsightsDtos.DailyReportOverviewResponse overview(String date, String timezone) {
        DailyReport report = getDailyReportOrThrow(date, timezone);
        DailyReportSummary summary = dailyReportSummaryRepository.findByDailyReportDailyReportId(report.getDailyReportId())
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "REPORT_NOT_AVAILABLE", "The requested report is not available"));

        return ReportsInsightsDtos.DailyReportOverviewResponse.builder()
            .data(
                ReportsInsightsDtos.DailyReportOverviewData.builder()
                    .title(report.getTitle())
                    .subtitle(report.getSubtitle())
                    .summary(
                        ReportsInsightsDtos.DailyReportSummary.builder()
                            .totalVisitors(summary.getTotalVisitors())
                            .totalVisitorsChangePct(summary.getTotalVisitorsChangePct() == null ? null : summary.getTotalVisitorsChangePct().intValue())
                            .peakTrafficWindow(summary.getPeakTrafficWindow())
                            .peakWindowSharePct(summary.getPeakWindowSharePct() == null ? null : summary.getPeakWindowSharePct().intValue())
                            .alertsTriggered(summary.getAlertsTriggered())
                            .alertsResolutionStatus(summary.getAlertsResolutionStatus())
                            .build()
                    )
                    .generatedAt(OffsetDateTime.ofInstant(report.getGeneratedAt(), ZoneId.systemDefault()))
                    .build()
            )
            .build();
    }

    public ReportsInsightsDtos.IntelligenceNarrativeResponse intelligence(String date, String timezone) {
        DailyReport report = getDailyReportOrThrow(date, timezone);
        DailyReportSummary summary = getOrCreateSummary(report);

        List<DailyReportAnomaly> anomalies = dailyReportAnomalyRepository.findByDailyReportDailyReportIdOrderBySortOrderAsc(report.getDailyReportId());
        List<DailyReportRecommendation> recommendations = dailyReportRecommendationRepository
            .findByDailyReportDailyReportIdOrderBySortOrderAsc(report.getDailyReportId());

        if (anomalies.isEmpty() && recommendations.isEmpty()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "INTELLIGENCE_NOT_READY", "Intelligence data is not ready");
        }

        String dailyPatterns = ensureDailyPatterns(report, summary, anomalies, recommendations);

        return ReportsInsightsDtos.IntelligenceNarrativeResponse.builder()
            .data(
                ReportsInsightsDtos.IntelligenceNarrativeData.builder()
                    .dailyPatterns(dailyPatterns)
                    .notableAnomalies(
                        anomalies.stream()
                            .map(a -> ReportsInsightsDtos.NotableAnomaly.builder()
                                .severity(a.getSeverity())
                                .description(a.getDescription())
                                .build())
                            .toList()
                    )
                    .build()
            )
            .build();
    }

    public ReportsInsightsDtos.ReportMetadataResponse metadata(String date, String timezone) {
        DailyReport report = getDailyReportOrThrow(date, timezone);
        List<DailyReportDataSource> dataSources = dailyReportDataSourceRepository
            .findByDailyReportDailyReportIdOrderBySortOrderAsc(report.getDailyReportId());

        return ReportsInsightsDtos.ReportMetadataResponse.builder()
            .data(
                ReportsInsightsDtos.ReportMetadataData.builder()
                    .author(report.getAuthor())
                    .dataSources(dataSources.stream().map(DailyReportDataSource::getSourceName).toList())
                    .confidenceScore(report.getConfidenceScore() == null ? null : report.getConfidenceScore().doubleValue())
                    .lastSyncAt(report.getLastSyncAt() == null ? null : OffsetDateTime.ofInstant(report.getLastSyncAt(), ZoneId.systemDefault()))
                    .activeZoneDensityMapUrl("https://cdn.vigi-gate.com/maps/density/" + report.getReportDate() + ".png")
                    .build()
            )
            .build();
    }

    public ResponseEntity<ReportsInsightsDtos.GenerateReportResponse> generate(ReportsInsightsDtos.GenerateReportRequest request) {
        LocalDate date = LocalDate.parse(request.getDate());

        DailyReport existing = dailyReportRepository.findByReportDateAndTimezone(date, request.getTimezone()).orElse(null);
        if (existing != null && Boolean.FALSE.equals(request.getForceRegenerate())) {
            DailyReportSummary existingSummary = getOrCreateSummary(existing);
            List<DailyReportAnomaly> anomalies = dailyReportAnomalyRepository
                .findByDailyReportDailyReportIdOrderBySortOrderAsc(existing.getDailyReportId());
            List<DailyReportRecommendation> recommendations = dailyReportRecommendationRepository
                .findByDailyReportDailyReportIdOrderBySortOrderAsc(existing.getDailyReportId());
            ensureDailyPatterns(existing, existingSummary, anomalies, recommendations);
            return ResponseEntity.ok(
                ReportsInsightsDtos.GenerateReportResponse.builder()
                    .data(
                        ReportsInsightsDtos.GenerateReportData.builder()
                            .status("READY")
                            .reportId(existing.getDailyReportId())
                            .generatedAt(OffsetDateTime.ofInstant(existing.getGeneratedAt(), ZoneId.systemDefault()))
                            .build()
                    )
                    .build()
            );
        }

        boolean inProgress = reportGenerationJobRepository.existsByReportDateAndTimezoneAndStatusIn(
            date,
            request.getTimezone(),
            Set.of("QUEUED", "RUNNING")
        );
        if (inProgress) {
            throw new ApiException(HttpStatus.CONFLICT, "REPORT_GENERATION_IN_PROGRESS", "Report generation is in progress");
        }

        Instant now = Instant.now();
        ReportGenerationJob job = reportGenerationJobRepository.save(
            ReportGenerationJob.builder()
                .reportDate(date)
                .timezone(request.getTimezone())
                .forceRegenerate(request.getForceRegenerate())
                .status("RUNNING")
                .requestedByUser(resolveActorUser())
                .queuedAt(now)
                .startedAt(now)
                .completedAt(null)
                .dailyReport(existing)
                .build()
        );

        try {
            DailyReport report = upsertReport(existing, date, request.getTimezone(), now);
            DailyReportSummary summary = getOrCreateSummary(report);
            recalculateSummaryFromOperationalData(report, summary);
            rebuildAnomaliesFromOperationalData(report);
            if (Boolean.TRUE.equals(request.getForceRegenerate())) {
                summary.setDailyPatterns(null);
                dailyReportSummaryRepository.save(summary);
            }

            List<DailyReportAnomaly> anomalies = dailyReportAnomalyRepository
                .findByDailyReportDailyReportIdOrderBySortOrderAsc(report.getDailyReportId());
            List<DailyReportRecommendation> recommendations = dailyReportRecommendationRepository
                .findByDailyReportDailyReportIdOrderBySortOrderAsc(report.getDailyReportId());

            if (Boolean.TRUE.equals(request.getForceRegenerate())) {
                regenerateDailyPatterns(report, summary, anomalies, recommendations);
            } else {
                ensureDailyPatterns(report, summary, anomalies, recommendations);
            }

            job.setDailyReport(report);
            job.setStatus("COMPLETED");
            job.setCompletedAt(Instant.now());
            reportGenerationJobRepository.save(job);

            return ResponseEntity.ok(
                ReportsInsightsDtos.GenerateReportResponse.builder()
                    .data(
                        ReportsInsightsDtos.GenerateReportData.builder()
                            .jobId(job.getJobId())
                            .status("READY")
                            .queuedAt(OffsetDateTime.ofInstant(job.getQueuedAt(), ZoneId.systemDefault()))
                            .reportId(report.getDailyReportId())
                            .generatedAt(OffsetDateTime.ofInstant(report.getGeneratedAt(), ZoneId.systemDefault()))
                            .build()
                    )
                    .build()
            );
        } catch (RuntimeException ex) {
            job.setStatus("FAILED");
            job.setCompletedAt(Instant.now());
            reportGenerationJobRepository.save(job);
            throw ex;
        }
    }

    public ReportsInsightsDtos.ExportPdfResponse exportPdf(ReportsInsightsDtos.ExportPdfRequest request) {
        LocalDate date = LocalDate.parse(request.getDate());
        DailyReport report = dailyReportRepository.findByReportDateAndTimezone(date, request.getTimezone())
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "REPORT_NOT_AVAILABLE", "The requested report is not available"));

        ReportExport existingExport = reportExportRepository
            .findTopByDailyReportDailyReportIdAndFormatOrderByCreatedAtDesc(report.getDailyReportId(), "pdf")
            .orElse(null);

        ReportExport export = existingExport;
        if (export == null) {
            Instant now = Instant.now();
            export = reportExportRepository.save(
                ReportExport.builder()
                    .dailyReport(report)
                    .exportedByUser(resolveActorUser())
                    .format("pdf")
                    .includeSectionsJson(request.getIncludeSections() == null ? "[]" : request.getIncludeSections().toString())
                    .downloadUrl(buildLocalDownloadUrl(report.getDailyReportId()))
                    .expiresAt(now.plusSeconds(7200))
                    .createdAt(now)
                    .build()
            );
        } else if (!isLocalDownloadUrl(export.getDownloadUrl(), report.getDailyReportId())) {
            export.setDownloadUrl(buildLocalDownloadUrl(report.getDailyReportId()));
            reportExportRepository.save(export);
        }

        return ReportsInsightsDtos.ExportPdfResponse.builder()
            .data(
                ReportsInsightsDtos.ExportPdfData.builder()
                    .reportId(report.getDailyReportId())
                    .format(export.getFormat())
                    .downloadUrl(export.getDownloadUrl())
                    .expiresAt(export.getExpiresAt() == null ? null : OffsetDateTime.ofInstant(export.getExpiresAt(), ZoneId.systemDefault()))
                    .build()
            )
            .build();
    }

    public ResponseEntity<byte[]> download(java.util.UUID reportId, String format) {
        if (!"pdf".equalsIgnoreCase(format)) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "UNSUPPORTED_REPORT_FORMAT", "Only pdf format is supported");
        }

        DailyReport report = dailyReportRepository.findById(reportId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "REPORT_NOT_AVAILABLE", "The requested report is not available"));
        DailyReportSummary summary = getOrCreateSummary(report);
        List<DailyReportAnomaly> anomalies = dailyReportAnomalyRepository
            .findByDailyReportDailyReportIdOrderBySortOrderAsc(report.getDailyReportId());
        List<DailyReportRecommendation> recommendations = dailyReportRecommendationRepository
            .findByDailyReportDailyReportIdOrderBySortOrderAsc(report.getDailyReportId());

        List<String> anomalyDescriptions = anomalies.stream()
            .map(item -> item.getSeverity() + ": " + item.getDescription())
            .toList();
        List<String> recommendationTexts = recommendations.stream()
            .map(DailyReportRecommendation::getRecommendationText)
            .toList();

        String dailyPatterns = ensureDailyPatterns(report, summary, anomalies, recommendations);
        String aiExecutiveBrief = generateAiExecutiveBrief(report, summary, anomalyDescriptions, recommendationTexts);

        byte[] pdfBytes = generateSimplePdf(report, summary, dailyPatterns, aiExecutiveBrief, anomalies, recommendations);

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"daily-report-" + report.getReportDate() + ".pdf\"")
            .contentType(MediaType.APPLICATION_PDF)
            .contentLength(pdfBytes.length)
            .body(pdfBytes);
    }

    private DailyReport getDailyReportOrThrow(String date, String timezone) {
        LocalDate parsed = LocalDate.parse(date);
        return dailyReportRepository.findByReportDateAndTimezone(parsed, timezone)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "REPORT_NOT_AVAILABLE", "The requested report is not available"));
    }

    private String buildLocalDownloadUrl(java.util.UUID reportId) {
        String normalized = apiBaseUrl == null ? "http://localhost:8080" : apiBaseUrl.trim();
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized + "/api/v1/reports/daily/" + reportId + "/download?format=pdf";
    }

    private boolean isLocalDownloadUrl(String url, java.util.UUID reportId) {
        if (url == null) {
            return false;
        }
        return url.contains("/api/v1/reports/daily/" + reportId + "/download");
    }

    private byte[] generateSimplePdf(
        DailyReport report,
        DailyReportSummary summary,
        String dailyPatterns,
        String aiExecutiveBrief,
        List<DailyReportAnomaly> anomalies,
        List<DailyReportRecommendation> recommendations
    ) {
        List<String> lines = new ArrayList<>(List.of(
            "Vigi-Gate Daily Report",
            "Date: " + report.getReportDate(),
            "Timezone: " + report.getTimezone(),
            "Title: " + safe(report.getTitle()),
            "Visitors: " + safeNumber(summary.getTotalVisitors()),
            "Alerts Triggered: " + safeNumber(summary.getAlertsTriggered()),
            "Peak Window: " + safe(summary.getPeakTrafficWindow())
        ));
        lines.add("AI Daily Pattern: " + safe(dailyPatterns));
        if (aiExecutiveBrief != null && !aiExecutiveBrief.isBlank()) {
            lines.add("AI Executive Brief: " + aiExecutiveBrief);
        }
        if (anomalies == null || anomalies.isEmpty()) {
            lines.add("Anomalies: none detected.");
        } else {
            anomalies.stream().limit(3).forEach(anomaly ->
                lines.add("Anomaly [" + safe(anomaly.getSeverity()) + "]: " + safe(anomaly.getDescription()))
            );
        }
        if (recommendations == null || recommendations.isEmpty()) {
            lines.add("Recommendations: none.");
        } else {
            recommendations.stream().limit(3).forEach(item ->
                lines.add("Recommendation: " + safe(item.getRecommendationText()))
            );
        }

        StringBuilder content = new StringBuilder();
        content.append("BT\n/F1 14 Tf\n72 760 Td\n");
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) {
                content.append("0 -20 Td\n");
            }
            content.append("(").append(escapePdfText(lines.get(i))).append(") Tj\n");
        }
        content.append("ET\n");

        byte[] contentBytes = content.toString().getBytes(StandardCharsets.ISO_8859_1);
        List<String> objects = new ArrayList<>();
        objects.add("<< /Type /Catalog /Pages 2 0 R >>");
        objects.add("<< /Type /Pages /Kids [3 0 R] /Count 1 >>");
        objects.add("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>");
        objects.add("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>");
        objects.add("<< /Length " + contentBytes.length + " >>\nstream\n"
            + new String(contentBytes, StandardCharsets.ISO_8859_1)
            + "endstream");

        StringBuilder pdf = new StringBuilder("%PDF-1.4\n");
        List<Integer> offsets = new ArrayList<>();
        for (int i = 0; i < objects.size(); i++) {
            offsets.add(pdf.length());
            pdf.append(i + 1).append(" 0 obj\n")
                .append(objects.get(i)).append("\nendobj\n");
        }

        int xrefOffset = pdf.length();
        pdf.append("xref\n0 ").append(objects.size() + 1).append("\n");
        pdf.append("0000000000 65535 f \n");
        for (Integer offset : offsets) {
            pdf.append(String.format(Locale.ROOT, "%010d 00000 n \n", offset));
        }
        pdf.append("trailer\n<< /Size ").append(objects.size() + 1).append(" /Root 1 0 R >>\n");
        pdf.append("startxref\n").append(xrefOffset).append("\n%%EOF");

        return pdf.toString().getBytes(StandardCharsets.ISO_8859_1);
    }

    private String escapePdfText(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }

    private String safe(String value) {
        return value == null ? "-" : value;
    }

    private String safeNumber(Number value) {
        return value == null ? "-" : String.valueOf(value);
    }

    private String generateAiExecutiveBrief(
        DailyReport report,
        DailyReportSummary summary,
        List<String> anomalyDescriptions,
        List<String> recommendationTexts
    ) {
        try {
            String generated = summaryGenerator.generateDailyPatterns(
                buildDailyPatternsInput(report, summary, anomalyDescriptions, recommendationTexts)
            );
            if (generated == null || generated.isBlank()) {
                return null;
            }
            return generated;
        } catch (RuntimeException ex) {
            log.warn("OpenAI executive brief generation failed for report {}: {}", report.getDailyReportId(), ex.getMessage());
            return null;
        }
    }

    private User resolveActorUser() {
        return userRepository.findByAccountStatus("ACTIVE").stream().findFirst()
            .orElseGet(() -> userRepository.save(
                User.builder()
                    .fullName("System User")
                    .email("system@vigi-gate.local")
                    .passwordHash("{noop}system")
                    .accountStatus("ACTIVE")
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .lastLoginAt(null)
                    .build()
            ));
    }

    private DailyReport upsertReport(DailyReport existing, LocalDate reportDate, String timezone, Instant generatedAt) {
        DailyReport report = existing;
        if (report == null) {
            report = DailyReport.builder()
                .reportDate(reportDate)
                .timezone(timezone)
                .title("Daily Security Intelligence")
                .subtitle("Comprehensive insights for " + reportDate)
                .author("Sentinal Core AI v4.2")
                .confidenceScore(BigDecimal.valueOf(98.4))
                .lastSyncAt(generatedAt)
                .generatedAt(generatedAt)
                .status("READY")
                .build();
        } else {
            report.setGeneratedAt(generatedAt);
            report.setLastSyncAt(generatedAt);
            report.setStatus("READY");
        }

        return dailyReportRepository.save(report);
    }

    private DailyReportSummary getOrCreateSummary(DailyReport report) {
        return dailyReportSummaryRepository.findByDailyReportDailyReportId(report.getDailyReportId())
            .orElseGet(() -> dailyReportSummaryRepository.save(
                DailyReportSummary.builder()
                    .dailyReport(report)
                    .totalVisitors(0)
                    .totalVisitorsChangePct(BigDecimal.ZERO)
                    .peakTrafficWindow("N/A")
                    .peakWindowSharePct(BigDecimal.ZERO)
                    .alertsTriggered(0)
                    .alertsResolutionStatus("NO_ALERTS")
                    .dailyPatterns(null)
                    .build()
            ));
    }

    private String ensureDailyPatterns(
        DailyReport report,
        DailyReportSummary summary,
        List<DailyReportAnomaly> anomalies,
        List<DailyReportRecommendation> recommendations
    ) {
        if (summary.getDailyPatterns() != null && !summary.getDailyPatterns().isBlank()) {
            return summary.getDailyPatterns();
        }

        List<String> anomalyDescriptions = anomalies.stream()
            .map(item -> item.getSeverity() + ": " + item.getDescription())
            .toList();
        List<String> recommendationTexts = recommendations.stream()
            .map(DailyReportRecommendation::getRecommendationText)
            .toList();

        String narrative;
        try {
            narrative = summaryGenerator.generateDailyPatterns(
                buildDailyPatternsInput(report, summary, anomalyDescriptions, recommendationTexts)
            );
        } catch (RuntimeException ex) {
            log.warn("OpenAI daily narrative generation failed, using fallback: {}", ex.getMessage());
            narrative = fallbackDailyPatterns(summary);
        }

        if (narrative == null || narrative.isBlank()) {
            narrative = fallbackDailyPatterns(summary);
        }

        summary.setDailyPatterns(narrative);
        dailyReportSummaryRepository.save(summary);
        return narrative;
    }

    private String regenerateDailyPatterns(
        DailyReport report,
        DailyReportSummary summary,
        List<DailyReportAnomaly> anomalies,
        List<DailyReportRecommendation> recommendations
    ) {
        summary.setDailyPatterns(null);
        dailyReportSummaryRepository.save(summary);

        List<String> anomalyDescriptions = anomalies.stream()
            .map(item -> item.getSeverity() + ": " + item.getDescription())
            .toList();
        List<String> recommendationTexts = recommendations.stream()
            .map(DailyReportRecommendation::getRecommendationText)
            .toList();

        String narrative;
        try {
            narrative = summaryGenerator.generateDailyPatterns(
                buildDailyPatternsInput(report, summary, anomalyDescriptions, recommendationTexts)
            );
        } catch (RuntimeException ex) {
            log.warn("OpenAI forced daily narrative regeneration failed, using fallback: {}", ex.getMessage());
            narrative = fallbackDailyPatterns(summary);
        }

        if (narrative == null || narrative.isBlank()) {
            narrative = fallbackDailyPatterns(summary);
        }

        summary.setDailyPatterns(narrative);
        dailyReportSummaryRepository.save(summary);
        return narrative;
    }

    private String fallbackDailyPatterns(DailyReportSummary summary) {
        int totalVisitors = summary.getTotalVisitors() == null ? 0 : summary.getTotalVisitors();
        int alerts = summary.getAlertsTriggered() == null ? 0 : summary.getAlertsTriggered();
        String peakWindow = summary.getPeakTrafficWindow() == null ? "N/A" : summary.getPeakTrafficWindow();

        return "Traffic recorded " + totalVisitors + " visitors with peak activity around "
            + peakWindow + ". Alert volume was " + alerts + ", continue applying standard security controls.";
    }

    private DailyPatternsInput buildDailyPatternsInput(
        DailyReport report,
        DailyReportSummary summary,
        List<String> anomalyDescriptions,
        List<String> recommendationTexts
    ) {
        ZoneId zoneId = ZoneId.of(report.getTimezone());
        ZonedDateTime dayStart = report.getReportDate().atStartOfDay(zoneId);
        Instant start = dayStart.toInstant();
        Instant end = dayStart.plusDays(1).toInstant();

        List<CheckIn> dayCheckins = checkInRepository.findByCheckinAtBetween(start, end);
        List<RiskAlert> dayAlerts = riskAlertRepository.findByCreatedAtBetween(start, end);

        long uniqueVisitors = dayCheckins.stream()
            .map(checkIn -> checkIn.getVisitor().getVisitorId())
            .distinct()
            .count();

        Map<String, Long> purposeCounts = dayCheckins.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                checkIn -> checkIn.getPurpose() == null ? "Unknown Purpose" : checkIn.getPurpose().getLabel(),
                java.util.stream.Collectors.counting()
            ));

        List<String> topPurposes = purposeCounts.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
            .limit(3)
            .map(entry -> entry.getKey() + "=" + entry.getValue())
            .toList();

        long openAlerts = dayAlerts.stream()
            .filter(alert -> "OPEN".equalsIgnoreCase(alert.getStatus()))
            .count();
        long resolvedAlerts = dayAlerts.stream()
            .filter(alert -> "RESOLVED".equalsIgnoreCase(alert.getStatus()) || "DISMISSED".equalsIgnoreCase(alert.getStatus()))
            .count();

        Map<String, Long> severityBreakdown = dayAlerts.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                alert -> alert.getSeverity() == null ? "UNKNOWN" : alert.getSeverity().toUpperCase(Locale.ROOT),
                java.util.stream.Collectors.counting()
            ));

        List<String> facts = new ArrayList<>();
        facts.add("total_checkins=" + dayCheckins.size());
        facts.add("unique_visitors=" + uniqueVisitors);
        facts.add("alerts_total=" + dayAlerts.size());
        facts.add("alerts_open=" + openAlerts);
        facts.add("alerts_resolved_or_dismissed=" + resolvedAlerts);
        facts.add("peak_window=" + safe(summary.getPeakTrafficWindow()));
        facts.add("peak_window_share_pct=" + safeNumber(summary.getPeakWindowSharePct()));
        if (!topPurposes.isEmpty()) {
            facts.add("top_purposes=" + String.join(", ", topPurposes));
        }
        if (!severityBreakdown.isEmpty()) {
            facts.add("alert_severity_breakdown=" + severityBreakdown);
        }

        return new DailyPatternsInput(
            report.getReportDate(),
            report.getTimezone(),
            summary.getTotalVisitors(),
            summary.getAlertsTriggered(),
            summary.getPeakTrafficWindow(),
            summary.getPeakWindowSharePct() == null ? null : summary.getPeakWindowSharePct().intValue(),
            summary.getAlertsResolutionStatus(),
            anomalyDescriptions,
            recommendationTexts,
            facts
        );
    }

    private void recalculateSummaryFromOperationalData(DailyReport report, DailyReportSummary summary) {
        ZoneId zoneId = ZoneId.of(report.getTimezone());
        ZonedDateTime dayStart = report.getReportDate().atStartOfDay(zoneId);
        Instant start = dayStart.toInstant();
        Instant end = dayStart.plusDays(1).toInstant();

        List<CheckIn> dayCheckins = checkInRepository.findByCheckinAtBetween(start, end);
        int totalVisitors = (int) dayCheckins.stream()
            .map(checkIn -> checkIn.getVisitor().getVisitorId())
            .distinct()
            .count();

        Map<Integer, Integer> hourlyBuckets = new HashMap<>();
        for (CheckIn checkIn : dayCheckins) {
            if (checkIn.getCheckinAt() == null) {
                continue;
            }
            int hour = LocalDateTime.ofInstant(checkIn.getCheckinAt(), zoneId).getHour();
            hourlyBuckets.merge(hour, 1, Integer::sum);
        }

        int peakStartHour = 9;
        int peakCount = 0;
        for (int hour = 0; hour < 24; hour++) {
            int windowCount = hourlyBuckets.getOrDefault(hour, 0) + hourlyBuckets.getOrDefault((hour + 1) % 24, 0);
            if (windowCount > peakCount) {
                peakCount = windowCount;
                peakStartHour = hour;
            }
        }

        String peakWindow = formatPeakWindow(peakStartHour, zoneId);
        int peakSharePct = totalVisitors == 0 ? 0 : (int) Math.round((peakCount * 100.0) / totalVisitors);

        List<RiskAlert> dayAlerts = riskAlertRepository.findByCreatedAtBetween(start, end);
        int alertsTriggered = dayAlerts.size();
        long closedAlerts = dayAlerts.stream()
            .filter(alert -> {
                String status = alert.getStatus();
                return "RESOLVED".equalsIgnoreCase(status) || "DISMISSED".equalsIgnoreCase(status);
            })
            .count();
        String resolutionStatus = alertsTriggered == 0
            ? "NO_ALERTS"
            : (closedAlerts == alertsTriggered ? "All Resolved" : "Open Alerts");

        summary.setTotalVisitors(totalVisitors);
        summary.setPeakTrafficWindow(peakWindow);
        summary.setPeakWindowSharePct(BigDecimal.valueOf(peakSharePct));
        summary.setAlertsTriggered(alertsTriggered);
        summary.setAlertsResolutionStatus(resolutionStatus);
        summary.setTotalVisitorsChangePct(BigDecimal.ZERO);
        dailyReportSummaryRepository.save(summary);
    }

    private String formatPeakWindow(int startHour, ZoneId zoneId) {
        ZonedDateTime base = ZonedDateTime.ofInstant(Instant.EPOCH, zoneId);
        String startLabel = base.withHour(startHour).withMinute(0).format(HOUR_LABEL_FORMAT);
        String endLabel = base.withHour((startHour + 2) % 24).withMinute(0).format(HOUR_LABEL_FORMAT);
        return startLabel + " - " + endLabel;
    }

    private void rebuildAnomaliesFromOperationalData(DailyReport report) {
        ZoneId zoneId = ZoneId.of(report.getTimezone());
        ZonedDateTime dayStart = report.getReportDate().atStartOfDay(zoneId);
        Instant start = dayStart.toInstant();
        Instant end = dayStart.plusDays(1).toInstant();

        List<RiskAlert> dayAlerts = riskAlertRepository.findByCreatedAtBetween(start, end);
        dailyReportAnomalyRepository.deleteByDailyReportDailyReportId(report.getDailyReportId());

        List<RiskAlert> topAlerts = dayAlerts.stream()
            .sorted((left, right) -> {
                int severityCompare = Integer.compare(severityRank(right.getSeverity()), severityRank(left.getSeverity()));
                if (severityCompare != 0) {
                    return severityCompare;
                }
                Instant leftTime = left.getCreatedAt() == null ? Instant.EPOCH : left.getCreatedAt();
                Instant rightTime = right.getCreatedAt() == null ? Instant.EPOCH : right.getCreatedAt();
                return rightTime.compareTo(leftTime);
            })
            .limit(3)
            .toList();

        int sortOrder = 1;
        for (RiskAlert alert : topAlerts) {
            String title = alert.getTitle() == null || alert.getTitle().isBlank() ? "Security anomaly detected" : alert.getTitle().trim();
            String context = alert.getDescription() == null || alert.getDescription().isBlank() ? "" : " (" + alert.getDescription().trim() + ")";
            String text = title + context;

            dailyReportAnomalyRepository.save(
                DailyReportAnomaly.builder()
                    .dailyReport(report)
                    .severity(normalizeSeverity(alert.getSeverity()))
                    .description(text)
                    .sortOrder(sortOrder++)
                    .build()
            );
        }
    }

    private int severityRank(String severity) {
        if (severity == null) {
            return 0;
        }
        return switch (severity.toUpperCase(Locale.ROOT)) {
            case "HIGH", "CRITICAL" -> 3;
            case "MEDIUM" -> 2;
            case "LOW" -> 1;
            default -> 0;
        };
    }

    private String normalizeSeverity(String severity) {
        if (severity == null || severity.isBlank()) {
            return "LOW";
        }
        return severity.toUpperCase(Locale.ROOT);
    }
}
