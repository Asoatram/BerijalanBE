package com.example.berijalanassesment.dto.screen;

import jakarta.validation.constraints.NotBlank;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public final class AdminDashboardDtos {

    private AdminDashboardDtos() {
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DashboardSummaryData {
        private Integer totalVisitorsToday;
        private Integer currentlyActive;
        private Integer highRiskVisitors;
        private Integer badgeQueueCount;
        private OffsetDateTime generatedAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DashboardSummaryResponse {
        private DashboardSummaryData data;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VisitorRow {
        private UUID sessionId;
        private UUID visitorId;
        private String name;
        private String email;
        private String nik;
        private String purposeLabel;
        private String checkinTime;
        private String status;
        private String riskLevel;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VisitorListMeta {
        private Integer page;
        private Integer pageSize;
        private Integer totalRecords;
        private Integer totalPages;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ListVisitorsResponse {
        private List<VisitorRow> data;
        private VisitorListMeta meta;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RiskAlertItem {
        private UUID alertId;
        private String severity;
        private String title;
        private String description;
        private OffsetDateTime createdAt;
        private String relativeTime;
        private String status;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RiskAlertsResponse {
        private List<RiskAlertItem> data;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RiskAlertActionRequest {
        @NotBlank
        private String action;

        private String note;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RiskAlertActionData {
        private UUID alertId;
        private String status;
        private String actedBy;
        private OffsetDateTime actedAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RiskAlertActionResponse {
        private RiskAlertActionData data;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuickRegisterVisitorRequest {
        @NotBlank
        private String fullName;

        @NotBlank
        private String nik;

        private UUID purposeId;
        private UUID photoId;

        @NotBlank
        private String deviceId;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuickRegisterVisitorData {
        private UUID checkinId;
        private String status;
        private OffsetDateTime checkinAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuickRegisterVisitorResponse {
        private QuickRegisterVisitorData data;
    }
}
