package com.example.berijalanassesment.dto.screen;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public final class AdminAccessLoginDtos {

    private AdminAccessLoginDtos() {
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AdminLoginRequest {
        @NotBlank
        @Email
        private String email;

        @NotBlank
        private String password;

        @NotNull
        private Boolean rememberMe;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuthenticatedUser {
        private UUID userId;
        private String name;
        private String email;
        private String role;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AdminLoginData {
        private String accessToken;
        private String refreshToken;
        private String tokenType;
        private Integer expiresIn;
        private AuthenticatedUser user;
        private Boolean mfaRequired;
        private String mfaToken;
        private List<String> methods;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AdminLoginResponse {
        private AdminLoginData data;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuthMeData {
        private UUID userId;
        private String name;
        private String email;
        private String role;
        private List<String> permissions;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuthMeResponse {
        private AuthMeData data;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RefreshTokenRequest {
        @NotBlank
        private String refreshToken;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RefreshTokenData {
        private String accessToken;
        private String tokenType;
        private Integer expiresIn;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RefreshTokenResponse {
        private RefreshTokenData data;
    }
}
