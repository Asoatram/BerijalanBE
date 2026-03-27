package com.example.berijalanassesment.dto.screen;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public final class ActiveVisitorSessionDtos {

    private ActiveVisitorSessionDtos() {
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VisitorIdentity {
        private UUID visitorId;
        private String fullName;
        private String nik;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SessionCheckin {
        private UUID checkinId;
        private Instant checkinAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SessionVisitIntent {
        private UUID purposeId;
        private String label;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ActiveSessionData {
        private UUID sessionId;
        private String status;
        private VisitorIdentity visitor;
        private SessionCheckin checkin;
        private SessionVisitIntent visitIntent;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ActiveSessionResponse {
        private ActiveSessionData data;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CheckOutRequest {
        @NotNull
        private UUID sessionId;

        @NotBlank
        private String checkoutBy;

        @NotBlank
        private String reason;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AccessRevocation {
        private Boolean temporaryKeysRevoked;
        private Instant revokedAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CheckOutData {
        private UUID checkoutId;
        private UUID sessionId;
        private String status;
        private Instant checkoutAt;
        private AccessRevocation access;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CheckOutResponse {
        private CheckOutData data;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SessionDetailData {
        private UUID sessionId;
        private String status;
        private Instant checkinAt;
        private Instant checkoutAt;
        private String visitorName;
        private String purposeLabel;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SessionDetailResponse {
        private SessionDetailData data;
    }
}
