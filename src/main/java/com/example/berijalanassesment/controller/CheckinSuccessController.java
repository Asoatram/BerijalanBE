package com.example.berijalanassesment.controller;

import com.example.berijalanassesment.dto.screen.CheckinSuccessDtos;
import com.example.berijalanassesment.service.CheckinSuccessService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/checkins")
public class CheckinSuccessController {

    private final CheckinSuccessService checkinSuccessService;

    public CheckinSuccessController(CheckinSuccessService checkinSuccessService) {
        this.checkinSuccessService = checkinSuccessService;
    }

    @GetMapping("/{checkinId}")
    public CheckinSuccessDtos.CheckinDetailResponse getCheckinDetail(@PathVariable UUID checkinId) {
        return checkinSuccessService.getCheckinDetail(checkinId);
    }

    @PostMapping("/{checkinId}/continue")
    public CheckinSuccessDtos.ContinueResponse continueFromSuccess(
        @PathVariable UUID checkinId,
        @Valid @RequestBody CheckinSuccessDtos.ContinueRequest request
    ) {
        return checkinSuccessService.continueFromSuccess(checkinId, request);
    }
}
