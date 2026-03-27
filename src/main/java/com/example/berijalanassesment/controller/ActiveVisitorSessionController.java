package com.example.berijalanassesment.controller;

import com.example.berijalanassesment.dto.screen.ActiveVisitorSessionDtos;
import com.example.berijalanassesment.service.ActiveVisitorSessionService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class ActiveVisitorSessionController {

    private final ActiveVisitorSessionService activeVisitorSessionService;

    public ActiveVisitorSessionController(ActiveVisitorSessionService activeVisitorSessionService) {
        this.activeVisitorSessionService = activeVisitorSessionService;
    }

    @GetMapping("/visitors/{visitorId}/active-session")
    public ActiveVisitorSessionDtos.ActiveSessionResponse getActiveSession(@PathVariable UUID visitorId) {
        return activeVisitorSessionService.getActiveSession(visitorId);
    }

    @PostMapping("/checkouts")
    public ResponseEntity<ActiveVisitorSessionDtos.CheckOutResponse> checkout(
        @Valid @RequestBody ActiveVisitorSessionDtos.CheckOutRequest request
    ) {
        return activeVisitorSessionService.checkout(request);
    }

    @GetMapping("/sessions/{sessionId}")
    @PreAuthorize("hasAuthority('SESSION_READ')")
    public ActiveVisitorSessionDtos.SessionDetailResponse getSessionDetail(@PathVariable UUID sessionId) {
        return activeVisitorSessionService.getSessionDetail(sessionId);
    }
}
