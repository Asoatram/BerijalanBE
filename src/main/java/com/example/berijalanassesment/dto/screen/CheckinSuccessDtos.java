package com.example.berijalanassesment.dto.screen;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public final class CheckinSuccessDtos {

    private CheckinSuccessDtos() {
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NextStep {
        private String code;
        private String title;
        private String message;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DigitalPass {
        private UUID passId;
        private String passNumber;
        private String status;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CheckinDetailData {
        private UUID checkinId;
        private String status;
        private Instant checkinAt;
        private NextStep nextStep;
        private DigitalPass digitalPass;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CheckinDetailResponse {
        private CheckinDetailData data;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ContinueRequest {
        @NotBlank
        private String action;

        @NotBlank
        private String deviceId;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ContinueData {
        private String nextRoute;
        private String nextScreen;
        private UUID checkinId;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ContinueResponse {
        private ContinueData data;
    }
}
