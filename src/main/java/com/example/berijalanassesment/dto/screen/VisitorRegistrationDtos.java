package com.example.berijalanassesment.dto.screen;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public final class VisitorRegistrationDtos {

    private VisitorRegistrationDtos() {
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VisitIntentItem {
        private UUID id;
        private String code;
        private String label;
        private Integer sortOrder;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VisitIntentsResponse {
        private List<VisitIntentItem> data;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PolicyRuleNik {
        private Boolean required;
        private Integer digits;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PolicyRuleFullName {
        private Boolean required;
        private Integer minLength;
        private Integer maxLength;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PolicyRulePhoto {
        private Boolean required;
        private List<String> allowedMimeTypes;
        private Long maxSizeBytes;
        private Double minQualityScore;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CheckinPolicy {
        private PolicyRuleNik nik;
        private PolicyRuleFullName fullName;
        private PolicyRulePhoto photo;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CheckinPolicyResponse {
        private CheckinPolicy data;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UploadPortraitRequest {
        @NotBlank
        private String fileName;

        @NotBlank
        private String mimeType;

        @NotBlank
        private String contentBase64;

        @NotBlank
        private String checksumSha256;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UploadPortraitData {
        private UUID photoId;
        private String status;
        private Double qualityScore;
        private Instant expiresAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UploadPortraitResponse {
        private UploadPortraitData data;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateCheckInRequest {
        @NotBlank
        @Size(min = 3, max = 150)
        private String fullName;

        @NotBlank
        @Pattern(regexp = "^\\d{16}$")
        private String nik;

        @NotNull
        private UUID purposeId;

        @NotNull
        private UUID photoId;

        @NotBlank
        private String deviceId;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateCheckInData {
        private UUID checkinId;
        private UUID visitorId;
        private UUID sessionId;
        private String status;
        private Instant checkinAt;
        private String message;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateCheckInResponse {
        private CreateCheckInData data;
    }
}
