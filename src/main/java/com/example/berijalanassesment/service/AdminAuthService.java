package com.example.berijalanassesment.service;

import com.example.berijalanassesment.controller.support.ApiException;
import com.example.berijalanassesment.dto.screen.AdminAccessLoginDtos;
import com.example.berijalanassesment.models.AuthRefreshToken;
import com.example.berijalanassesment.models.Role;
import com.example.berijalanassesment.models.RolePermission;
import com.example.berijalanassesment.models.User;
import com.example.berijalanassesment.models.UserRole;
import com.example.berijalanassesment.repository.AuthRefreshTokenRepository;
import com.example.berijalanassesment.repository.RolePermissionRepository;
import com.example.berijalanassesment.repository.UserRepository;
import com.example.berijalanassesment.repository.UserRoleRepository;
import com.example.berijalanassesment.security.JwtService;
import com.example.berijalanassesment.security.SecurityJwtProperties;
import jakarta.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class AdminAuthService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final AuthRefreshTokenRepository authRefreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SecurityJwtProperties jwtProperties;

    public AdminAuthService(
        UserRepository userRepository,
        UserRoleRepository userRoleRepository,
        RolePermissionRepository rolePermissionRepository,
        AuthRefreshTokenRepository authRefreshTokenRepository,
        PasswordEncoder passwordEncoder,
        JwtService jwtService,
        SecurityJwtProperties jwtProperties
    ) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.authRefreshTokenRepository = authRefreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
    }

    public AdminAccessLoginDtos.AdminLoginResponse adminLogin(
        AdminAccessLoginDtos.AdminLoginRequest request,
        String clientId
    ) {
        User user = userRepository.findByEmailIgnoreCase(request.getEmail())
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Email or password is incorrect"));

        if (!"ACTIVE".equalsIgnoreCase(user.getAccountStatus())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "ACCOUNT_DISABLED", "Account is not active");
        }

        boolean passwordMatches = passwordMatches(request.getPassword(), user.getPasswordHash());
        if (!passwordMatches) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Email or password is incorrect");
        }

        if (shouldUpgradePasswordHash(user.getPasswordHash())) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        user.setLastLoginAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);

        Role role = resolvePrimaryRole(user.getUserId());
        List<String> permissions = resolvePermissions(user.getUserId());

        String accessToken = jwtService.issueAccessToken(
            user.getUserId(),
            user.getEmail(),
            role == null ? "UNKNOWN" : role.getCode(),
            permissions
        );
        String refreshToken = "rft_" + UUID.randomUUID();

        authRefreshTokenRepository.save(
            AuthRefreshToken.builder()
                .user(user)
                .tokenHash(hashToken(refreshToken))
                .expiresAt(Instant.now().plusSeconds(resolveRefreshTtlSeconds(request.getRememberMe())))
                .revokedAt(null)
                .clientId(clientId)
                .createdAt(Instant.now())
                .build()
        );

        return AdminAccessLoginDtos.AdminLoginResponse.builder()
            .data(
                AdminAccessLoginDtos.AdminLoginData.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn((int) jwtProperties.getAccessTtlSeconds())
                    .user(
                        AdminAccessLoginDtos.AuthenticatedUser.builder()
                            .userId(user.getUserId())
                            .name(user.getFullName())
                            .email(user.getEmail())
                            .role(role == null ? "UNKNOWN" : role.getCode())
                            .build()
                    )
                    .mfaRequired(false)
                    .build()
            )
            .build();
    }

    public AdminAccessLoginDtos.AuthMeResponse me(String authorization) {
        Jwt jwt = decodeAuthorization(authorization);
        UUID userId = parseSubjectAsUuid(jwt.getSubject());
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "TOKEN_INVALID_OR_EXPIRED", "Token is invalid or expired"));

        List<UserRole> userRoles = userRoleRepository.findByUserUserId(userId);
        Role role = userRoles.stream()
            .map(UserRole::getRole)
            .sorted(Comparator.comparing(Role::getCode))
            .findFirst()
            .orElse(null);

        List<String> permissions = resolvePermissions(userId);

        return AdminAccessLoginDtos.AuthMeResponse.builder()
            .data(
                AdminAccessLoginDtos.AuthMeData.builder()
                    .userId(user.getUserId())
                    .name(user.getFullName())
                    .email(user.getEmail())
                    .role(role == null ? "UNKNOWN" : role.getCode())
                    .permissions(permissions)
                    .build()
            )
            .build();
    }

    public AdminAccessLoginDtos.RefreshTokenResponse refresh(AdminAccessLoginDtos.RefreshTokenRequest request) {
        AuthRefreshToken token = authRefreshTokenRepository.findByTokenHashAndRevokedAtIsNull(hashToken(request.getRefreshToken()))
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_INVALID", "Refresh token is invalid"));

        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_EXPIRED", "Refresh token has expired");
        }

        User user = token.getUser();
        Role role = resolvePrimaryRole(user.getUserId());
        List<String> permissions = resolvePermissions(user.getUserId());

        String accessToken = jwtService.issueAccessToken(
            user.getUserId(),
            user.getEmail(),
            role == null ? "UNKNOWN" : role.getCode(),
            permissions
        );

        return AdminAccessLoginDtos.RefreshTokenResponse.builder()
            .data(
                AdminAccessLoginDtos.RefreshTokenData.builder()
                    .accessToken(accessToken)
                    .tokenType("Bearer")
                    .expiresIn((int) jwtProperties.getAccessTtlSeconds())
                    .build()
            )
            .build();
    }

    private boolean passwordMatches(String plainPassword, String storedValue) {
        if (storedValue == null) {
            return false;
        }
        if (storedValue.startsWith("{noop}")) {
            return plainPassword.equals(storedValue.substring("{noop}".length()));
        }
        if (storedValue.startsWith("$2a$") || storedValue.startsWith("$2b$") || storedValue.startsWith("$2y$")) {
            return passwordEncoder.matches(plainPassword, storedValue);
        }
        return plainPassword.equals(storedValue);
    }

    private boolean shouldUpgradePasswordHash(String storedValue) {
        if (storedValue == null) {
            return false;
        }
        return !(storedValue.startsWith("$2a$") || storedValue.startsWith("$2b$") || storedValue.startsWith("$2y$"));
    }

    private Role resolvePrimaryRole(UUID userId) {
        return userRoleRepository.findByUserUserId(userId).stream()
            .map(UserRole::getRole)
            .filter(Objects::nonNull)
            .sorted(Comparator.comparing(role -> role.getCode() == null ? "" : role.getCode()))
            .findFirst()
            .orElse(null);
    }

    private List<String> resolvePermissions(UUID userId) {
        return userRoleRepository.findByUserUserId(userId).stream()
            .map(UserRole::getRole)
            .filter(Objects::nonNull)
            .map(Role::getRoleId)
            .filter(Objects::nonNull)
            .distinct()
            .flatMap(roleId -> rolePermissionRepository.findByRoleRoleId(roleId).stream())
            .map(RolePermission::getPermission)
            .filter(Objects::nonNull)
            .map(permission -> permission.getCode())
            .filter(code -> code != null && !code.isBlank())
            .distinct()
            .sorted()
            .toList();
    }

    private Jwt decodeAuthorization(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "TOKEN_INVALID_OR_EXPIRED", "Token is invalid or expired");
        }
        String token = authorizationHeader.substring("Bearer ".length()).trim();
        if (token.isEmpty()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "TOKEN_INVALID_OR_EXPIRED", "Token is invalid or expired");
        }
        try {
            return jwtService.decodeAccessToken(token);
        } catch (RuntimeException ex) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "TOKEN_INVALID_OR_EXPIRED", "Token is invalid or expired");
        }
    }

    private UUID parseSubjectAsUuid(String subject) {
        try {
            return UUID.fromString(subject);
        } catch (IllegalArgumentException ex) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "TOKEN_INVALID_OR_EXPIRED", "Token is invalid or expired");
        }
    }

    private long resolveRefreshTtlSeconds(Boolean rememberMe) {
        return Boolean.TRUE.equals(rememberMe)
            ? jwtProperties.getRefreshRemembermeTtlSeconds()
            : jwtProperties.getRefreshTtlSeconds();
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encoded = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(encoded.length * 2);
            for (byte b : encoded) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }
}
