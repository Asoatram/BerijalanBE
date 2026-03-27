package com.example.berijalanassesment.dto.screen;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public final class ReportsInsightsDtos {

    private ReportsInsightsDtos() {
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailyReportSummary {
        private Integer totalVisitors;
        private Integer totalVisitorsChangePct;
        private String peakTrafficWindow;
        private Integer peakWindowSharePct;
        private Integer alertsTriggered;
        private String alertsResolutionStatus;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailyReportOverviewData {
        private String title;
        private String subtitle;
        private DailyReportSummary summary;
        private OffsetDateTime generatedAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailyReportOverviewResponse {
        private DailyReportOverviewData data;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NotableAnomaly {
        private String severity;
        private String description;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class IntelligenceNarrativeData {
        private String dailyPatterns;
        private List<NotableAnomaly> notableAnomalies;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class IntelligenceNarrativeResponse {
        private IntelligenceNarrativeData data;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReportMetadataData {
        private String author;
        private List<String> dataSources;
        private Double confidenceScore;
        private OffsetDateTime lastSyncAt;
        private String activeZoneDensityMapUrl;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReportMetadataResponse {
        private ReportMetadataData data;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GenerateReportRequest {
        @NotBlank
        private String date;

        @NotBlank
        private String timezone;

        @NotNull
        private Boolean forceRegenerate;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GenerateReportData {
        private UUID jobId;
        private String status;
        private OffsetDateTime queuedAt;
        private UUID reportId;
        private OffsetDateTime generatedAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GenerateReportResponse {
        private GenerateReportData data;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExportPdfRequest {
        @NotBlank
        private String date;

        @NotBlank
        private String timezone;

        private List<String> includeSections;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExportPdfData {
        private UUID reportId;
        private String format;
        private String downloadUrl;
        private OffsetDateTime expiresAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExportPdfResponse {
        private ExportPdfData data;
    }
}
