package com.example.berijalanassesment.controller;

import com.example.berijalanassesment.dto.screen.AdminDashboardDtos;
import com.example.berijalanassesment.service.AdminDashboardService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    public AdminDashboardController(AdminDashboardService adminDashboardService) {
        this.adminDashboardService = adminDashboardService;
    }

    @GetMapping("/dashboard/summary")
    @PreAuthorize("hasAuthority('DASHBOARD_READ')")
    public AdminDashboardDtos.DashboardSummaryResponse getSummary(
        @RequestParam String date,
        @RequestParam(defaultValue = "Asia/Jakarta") String timezone
    ) {
        return adminDashboardService.getSummary(date, timezone);
    }

    @GetMapping("/visitors")
    @PreAuthorize("hasAuthority('VISITOR_READ')")
    public AdminDashboardDtos.ListVisitorsResponse listVisitors(
        @RequestParam(required = false) String query,
        @RequestParam(defaultValue = "ALL") String status,
        @RequestParam(defaultValue = "ALL") String riskLevel,
        @RequestParam(defaultValue = "today") String timeframe,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int pageSize
    ) {
        return adminDashboardService.listVisitors(query, status, riskLevel, timeframe, page, pageSize);
    }

    @GetMapping("/risk-alerts")
    @PreAuthorize("hasAuthority('RISK_ALERT_READ')")
    public AdminDashboardDtos.RiskAlertsResponse getRiskAlerts(
        @RequestParam(defaultValue = "OPEN") String status,
        @RequestParam(defaultValue = "5") int limit
    ) {
        return adminDashboardService.getRiskAlerts(status, limit);
    }

    @PostMapping("/risk-alerts/{alertId}/actions")
    @PreAuthorize("hasAuthority('RISK_ALERT_ACTION')")
    public AdminDashboardDtos.RiskAlertActionResponse handleRiskAlertAction(
        @PathVariable UUID alertId,
        @Valid @RequestBody AdminDashboardDtos.RiskAlertActionRequest request
    ) {
        return adminDashboardService.handleRiskAlertAction(alertId, request);
    }

    @PostMapping("/dashboard/quick-register")
    @PreAuthorize("hasAuthority('VISITOR_REGISTER')")
    public ResponseEntity<AdminDashboardDtos.QuickRegisterVisitorResponse> quickRegister(
        @Valid @RequestBody AdminDashboardDtos.QuickRegisterVisitorRequest request
    ) {
        return adminDashboardService.quickRegister(request);
    }
}
