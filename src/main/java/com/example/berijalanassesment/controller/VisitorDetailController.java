package com.example.berijalanassesment.controller;

import com.example.berijalanassesment.dto.screen.VisitorDetailViewDtos;
import com.example.berijalanassesment.service.VisitorDetailService;
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
@RequestMapping("/api/v1/sessions")
public class VisitorDetailController {

    private final VisitorDetailService visitorDetailService;

    public VisitorDetailController(VisitorDetailService visitorDetailService) {
        this.visitorDetailService = visitorDetailService;
    }

    @GetMapping("/{sessionId}/detail")
    @PreAuthorize("hasAuthority('SESSION_READ')")
    public VisitorDetailViewDtos.SessionDetailResponse getSessionDetail(@PathVariable UUID sessionId) {
        return visitorDetailService.getSessionDetail(sessionId);
    }

    @PostMapping("/{sessionId}/print-pass")
    @PreAuthorize("hasAuthority('SESSION_PRINT_PASS')")
    public ResponseEntity<VisitorDetailViewDtos.PrintPassResponse> printPass(
        @PathVariable UUID sessionId,
        @Valid @RequestBody VisitorDetailViewDtos.PrintPassRequest request
    ) {
        return visitorDetailService.printPass(sessionId, request);
    }

    @PostMapping("/{sessionId}/contact-host")
    @PreAuthorize("hasAuthority('SESSION_CONTACT_HOST')")
    public VisitorDetailViewDtos.ContactHostResponse contactHost(
        @PathVariable UUID sessionId,
        @Valid @RequestBody VisitorDetailViewDtos.ContactHostRequest request
    ) {
        return visitorDetailService.contactHost(sessionId, request);
    }

    @PostMapping("/{sessionId}/force-checkout")
    @PreAuthorize("hasAuthority('SESSION_FORCE_CHECKOUT')")
    public ResponseEntity<VisitorDetailViewDtos.ForceCheckoutResponse> forceCheckout(
        @PathVariable UUID sessionId,
        @Valid @RequestBody VisitorDetailViewDtos.ForceCheckoutRequest request
    ) {
        return visitorDetailService.forceCheckout(sessionId, request);
    }

    @GetMapping("/{sessionId}/report")
    @PreAuthorize("hasAuthority('SESSION_REPORT_READ')")
    public VisitorDetailViewDtos.DownloadReportResponse downloadSessionReport(
        @PathVariable UUID sessionId,
        @RequestParam(defaultValue = "pdf") String format
    ) {
        return visitorDetailService.downloadSessionReport(sessionId, format);
    }

    @PostMapping("/{sessionId}/flags")
    @PreAuthorize("hasAuthority('SESSION_FLAG')")
    public ResponseEntity<VisitorDetailViewDtos.FlagSessionResponse> flagSession(
        @PathVariable UUID sessionId,
        @Valid @RequestBody VisitorDetailViewDtos.FlagSessionRequest request
    ) {
        return visitorDetailService.flagSession(sessionId, request);
    }
}
