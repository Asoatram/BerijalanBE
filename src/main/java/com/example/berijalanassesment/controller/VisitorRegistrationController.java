package com.example.berijalanassesment.controller;

import com.example.berijalanassesment.dto.screen.VisitorRegistrationDtos;
import com.example.berijalanassesment.service.VisitorRegistrationService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1")
public class VisitorRegistrationController {

    private final VisitorRegistrationService visitorRegistrationService;

    public VisitorRegistrationController(VisitorRegistrationService visitorRegistrationService) {
        this.visitorRegistrationService = visitorRegistrationService;
    }

    @GetMapping("/visit-intents")
    public VisitorRegistrationDtos.VisitIntentsResponse getVisitIntents(
        @RequestParam(defaultValue = "true") boolean active
    ) {
        return visitorRegistrationService.getVisitIntents(active);
    }

    @GetMapping("/checkins/policy")
    public VisitorRegistrationDtos.CheckinPolicyResponse getCheckinPolicy() {
        return visitorRegistrationService.getCheckinPolicy();
    }

    @PostMapping("/media/portraits")
    public ResponseEntity<VisitorRegistrationDtos.UploadPortraitResponse> uploadPortrait(
        @Valid @RequestBody VisitorRegistrationDtos.UploadPortraitRequest request
    ) {
        return visitorRegistrationService.uploadPortrait(request);
    }

    @PostMapping(value = "/checkins", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VisitorRegistrationDtos.CreateCheckInResponse> createCheckIn(
        @RequestParam("fullName") String fullName,
        @RequestParam("nik") String nik,
        @RequestParam("purposeId") UUID purposeId,
        @RequestParam("deviceId") String deviceId,
        @RequestParam("photo") MultipartFile photo,
        @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        return visitorRegistrationService.createCheckInMultipart(
            fullName,
            nik,
            purposeId,
            deviceId,
            photo,
            idempotencyKey
        );
    }
}
