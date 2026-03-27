package com.example.berijalanassesment.controller;

import com.example.berijalanassesment.dto.screen.ReportsInsightsDtos;
import com.example.berijalanassesment.service.ReportsInsightsService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
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
@RequestMapping("/api/v1/reports/daily")
public class ReportsInsightsController {

    private final ReportsInsightsService reportsInsightsService;

    public ReportsInsightsController(ReportsInsightsService reportsInsightsService) {
        this.reportsInsightsService = reportsInsightsService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('REPORT_READ')")
    public ReportsInsightsDtos.DailyReportOverviewResponse overview(
        @RequestParam String date,
        @RequestParam(defaultValue = "Asia/Jakarta") String timezone
    ) {
        return reportsInsightsService.overview(date, timezone);
    }

    @GetMapping("/intelligence")
    @PreAuthorize("hasAuthority('REPORT_READ')")
    public ReportsInsightsDtos.IntelligenceNarrativeResponse intelligence(
        @RequestParam String date,
        @RequestParam(defaultValue = "Asia/Jakarta") String timezone
    ) {
        return reportsInsightsService.intelligence(date, timezone);
    }

    @GetMapping("/metadata")
    @PreAuthorize("hasAuthority('REPORT_READ')")
    public ReportsInsightsDtos.ReportMetadataResponse metadata(
        @RequestParam String date,
        @RequestParam(defaultValue = "Asia/Jakarta") String timezone
    ) {
        return reportsInsightsService.metadata(date, timezone);
    }

    @GetMapping("/{reportId}/download")
    @PreAuthorize("hasAuthority('REPORT_READ')")
    public ResponseEntity<byte[]> download(
        @PathVariable UUID reportId,
        @RequestParam(defaultValue = "pdf") String format
    ) {
        return reportsInsightsService.download(reportId, format);
    }

    @PostMapping("/generate")
    @PreAuthorize("hasAuthority('REPORT_GENERATE')")
    public ResponseEntity<ReportsInsightsDtos.GenerateReportResponse> generate(
        @Valid @RequestBody ReportsInsightsDtos.GenerateReportRequest request
    ) {
        return reportsInsightsService.generate(request);
    }

    @PostMapping("/export-pdf")
    @PreAuthorize("hasAuthority('REPORT_EXPORT')")
    public ReportsInsightsDtos.ExportPdfResponse exportPdf(
        @Valid @RequestBody ReportsInsightsDtos.ExportPdfRequest request
    ) {
        return reportsInsightsService.exportPdf(request);
    }
}
