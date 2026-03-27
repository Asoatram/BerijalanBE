package com.example.berijalanassesment.controller;

import com.example.berijalanassesment.dto.screen.AdminAccessLoginDtos;
import com.example.berijalanassesment.service.AdminAuthService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    public AdminAuthController(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    @PostMapping("/admin/login")
    public AdminAccessLoginDtos.AdminLoginResponse adminLogin(
        @Valid @RequestBody AdminAccessLoginDtos.AdminLoginRequest request,
        @RequestHeader(value = "X-Client-Id", required = false) String clientId
    ) {
        return adminAuthService.adminLogin(request, clientId);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public AdminAccessLoginDtos.AuthMeResponse me(
        @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        return adminAuthService.me(authorization);
    }

    @PostMapping("/refresh")
    public AdminAccessLoginDtos.RefreshTokenResponse refresh(
        @Valid @RequestBody AdminAccessLoginDtos.RefreshTokenRequest request
    ) {
        return adminAuthService.refresh(request);
    }
}
