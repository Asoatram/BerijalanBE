package com.example.berijalanassesment.service;

import com.example.berijalanassesment.controller.support.ApiException;
import com.example.berijalanassesment.dto.screen.CheckinSuccessDtos;
import com.example.berijalanassesment.models.CheckIn;
import com.example.berijalanassesment.models.DigitalPass;
import com.example.berijalanassesment.models.VisitSession;
import com.example.berijalanassesment.repository.CheckInRepository;
import com.example.berijalanassesment.repository.DigitalPassRepository;
import com.example.berijalanassesment.repository.VisitSessionRepository;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class CheckinSuccessService {

    private final CheckInRepository checkInRepository;
    private final VisitSessionRepository visitSessionRepository;
    private final DigitalPassRepository digitalPassRepository;

    public CheckinSuccessService(
        CheckInRepository checkInRepository,
        VisitSessionRepository visitSessionRepository,
        DigitalPassRepository digitalPassRepository
    ) {
        this.checkInRepository = checkInRepository;
        this.visitSessionRepository = visitSessionRepository;
        this.digitalPassRepository = digitalPassRepository;
    }

    public CheckinSuccessDtos.CheckinDetailResponse getCheckinDetail(UUID checkinId) {
        CheckIn checkIn = checkInRepository.findById(checkinId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "CHECKIN_NOT_FOUND", "Check-in record was not found"));

        VisitSession session = visitSessionRepository.findByCheckInCheckinId(checkinId).orElse(null);
        DigitalPass pass = session == null ? null : digitalPassRepository.findBySessionSessionId(session.getSessionId()).orElse(null);

        return CheckinSuccessDtos.CheckinDetailResponse.builder()
            .data(
                CheckinSuccessDtos.CheckinDetailData.builder()
                    .checkinId(checkIn.getCheckinId())
                    .status(checkIn.getStatus())
                    .checkinAt(checkIn.getCheckinAt())
                    .nextStep(
                        CheckinSuccessDtos.NextStep.builder()
                            .code("GO_TO_SECURITY_DESK")
                            .title("NEXT STEP")
                            .message("Please proceed to the security desk for further assistance. A digital pass has been issued to your profile.")
                            .build()
                    )
                    .digitalPass(
                        pass == null
                            ? null
                            : CheckinSuccessDtos.DigitalPass.builder()
                                .passId(pass.getPassId())
                                .passNumber(pass.getPassNumber())
                                .status(pass.getStatus())
                                .build()
                    )
                    .build()
            )
            .build();
    }

    public CheckinSuccessDtos.ContinueResponse continueFromSuccess(
        UUID checkinId,
        CheckinSuccessDtos.ContinueRequest request
    ) {
        checkInRepository.findById(checkinId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "CHECKIN_NOT_FOUND", "Check-in record was not found"));

        VisitSession session = visitSessionRepository.findByCheckInCheckinId(checkinId).orElse(null);
        DigitalPass pass = session == null ? null : digitalPassRepository.findBySessionSessionId(session.getSessionId()).orElse(null);

        String nextRoute = pass == null ? "/home" : "/digital-pass/" + pass.getPassId();
        String nextScreen = pass == null ? "HOME" : "DIGITAL_PASS";

        return CheckinSuccessDtos.ContinueResponse.builder()
            .data(
                CheckinSuccessDtos.ContinueData.builder()
                    .nextRoute(nextRoute)
                    .nextScreen(nextScreen)
                    .checkinId(checkinId)
                    .build()
            )
            .build();
    }
}
