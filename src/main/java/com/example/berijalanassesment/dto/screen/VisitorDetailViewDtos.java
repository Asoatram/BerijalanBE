package com.example.berijalanassesment.dto.screen;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public final class VisitorDetailViewDtos {

    private VisitorDetailViewDtos() {
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SessionVisitor {
        private UUID visitorId;
        private String fullName;
        private String avatarUrl;
        private String identityLabel;
        private String nik;
        private String kycStatus;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SessionVisit {
        private String purposeLabel;
        private String purposeSubtitle;
        private OffsetDateTime checkinAt;
        private Integer durationMinutes;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SessionRiskAnalysis {
        private String riskLevel;
        private Integer riskScore;
        private String summary;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SessionActions {
        private Boolean canPrintPass;
        private Boolean canContactHost;
        private Boolean canForceCheckout;
        private Boolean canFlagSession;
        private Boolean canDownloadReport;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SessionDetailData {
        private UUID sessionId;
        private String status;
        private SessionVisitor visitor;
        private SessionVisit visit;
        private SessionRiskAnalysis riskAnalysis;
        private SessionActions actions;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SessionDetailResponse {
        private SessionDetailData data;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PrintPassRequest {
        @NotBlank
        private String printerId;

        @NotNull
        @Min(1)
        private Integer copies;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PrintPassData {
        private UUID jobId;
        private String status;
        private OffsetDateTime queuedAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PrintPassResponse {
        private PrintPassData data;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ContactHostRequest {
        @NotBlank
        private String channel;

        @NotBlank
        private String message;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ContactHostData {
        private UUID notificationId;
        private String status;
        private OffsetDateTime sentAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ContactHostResponse {
        private ContactHostData data;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ForceCheckoutRequest {
        @NotBlank
        private String reasonCode;

        @NotBlank
        private String reasonNote;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ForceCheckoutData {
        private UUID checkoutId;
        private UUID sessionId;
        private String status;
        private OffsetDateTime checkoutAt;
        private Boolean accessRevoked;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ForceCheckoutResponse {
        private ForceCheckoutData data;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DownloadReportData {
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
    public static class DownloadReportResponse {
        private DownloadReportData data;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FlagSessionRequest {
        @NotBlank
        private String flagType;

        @NotBlank
        private String note;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FlagSessionData {
        private UUID flagId;
        private UUID sessionId;
        private String status;
        private OffsetDateTime createdAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FlagSessionResponse {
        private FlagSessionData data;
    }
}
