package com.example.berijalanassesment.service;

import com.example.berijalanassesment.controller.support.ApiException;
import com.example.berijalanassesment.dto.screen.ActiveVisitorSessionDtos;
import com.example.berijalanassesment.models.AccessKey;
import com.example.berijalanassesment.models.CheckOut;
import com.example.berijalanassesment.models.Device;
import com.example.berijalanassesment.models.VisitSession;
import com.example.berijalanassesment.repository.AccessKeyRepository;
import com.example.berijalanassesment.repository.CheckOutRepository;
import com.example.berijalanassesment.repository.DeviceRepository;
import com.example.berijalanassesment.repository.VisitSessionRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ActiveVisitorSessionService {

    private final VisitSessionRepository visitSessionRepository;
    private final CheckOutRepository checkOutRepository;
    private final AccessKeyRepository accessKeyRepository;
    private final DeviceRepository deviceRepository;

    public ActiveVisitorSessionService(
        VisitSessionRepository visitSessionRepository,
        CheckOutRepository checkOutRepository,
        AccessKeyRepository accessKeyRepository,
        DeviceRepository deviceRepository
    ) {
        this.visitSessionRepository = visitSessionRepository;
        this.checkOutRepository = checkOutRepository;
        this.accessKeyRepository = accessKeyRepository;
        this.deviceRepository = deviceRepository;
    }

    @Transactional(readOnly = true)
    public ActiveVisitorSessionDtos.ActiveSessionResponse getActiveSession(UUID visitorId) {
        VisitSession session = visitSessionRepository.findByVisitorVisitorIdAndStatus(visitorId, "ACTIVE")
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ACTIVE_SESSION_NOT_FOUND", "Active visitor session not found"));

        return ActiveVisitorSessionDtos.ActiveSessionResponse.builder()
            .data(
                ActiveVisitorSessionDtos.ActiveSessionData.builder()
                    .sessionId(session.getSessionId())
                    .status(session.getStatus())
                    .visitor(
                        ActiveVisitorSessionDtos.VisitorIdentity.builder()
                            .visitorId(session.getVisitor().getVisitorId())
                            .fullName(session.getVisitor().getFullName())
                            .nik(session.getVisitor().getNik())
                            .build()
                    )
                    .checkin(
                        ActiveVisitorSessionDtos.SessionCheckin.builder()
                            .checkinId(session.getCheckIn().getCheckinId())
                            .checkinAt(session.getCheckIn().getCheckinAt())
                            .build()
                    )
                    .visitIntent(
                        ActiveVisitorSessionDtos.SessionVisitIntent.builder()
                            .purposeId(session.getCheckIn().getPurpose().getPurposeId())
                            .label(session.getCheckIn().getPurpose().getLabel())
                            .build()
                    )
                    .build()
            )
            .build();
    }

    public ResponseEntity<ActiveVisitorSessionDtos.CheckOutResponse> checkout(ActiveVisitorSessionDtos.CheckOutRequest request) {
        VisitSession session = visitSessionRepository.findById(request.getSessionId())
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ACTIVE_SESSION_NOT_FOUND", "Active visitor session not found"));

        if (checkOutRepository.existsBySessionSessionId(session.getSessionId()) || !"ACTIVE".equalsIgnoreCase(session.getStatus())) {
            throw new ApiException(HttpStatus.CONFLICT, "SESSION_ALREADY_CHECKED_OUT", "Session has already been checked out");
        }

        Device device = deviceRepository.findById(request.getCheckoutBy()).orElseGet(() -> deviceRepository.save(
            Device.builder()
                .deviceId(request.getCheckoutBy())
                .deviceType("SECURITY_DESK")
                .locationLabel("Unknown")
                .status("ACTIVE")
                .lastSeenAt(Instant.now())
                .build()
        ));

        Instant now = Instant.now();

        CheckOut checkout = checkOutRepository.save(
            CheckOut.builder()
                .session(session)
                .performedByUser(null)
                .performedByDevice(device)
                .reasonCode(request.getReason())
                .reasonNote(null)
                .isForced(false)
                .status("CHECKED_OUT")
                .checkoutAt(now)
                .createdAt(now)
                .build()
        );

        session.setStatus("CHECKED_OUT");
        session.setCheckoutAt(now);
        session.setUpdatedAt(now);
        visitSessionRepository.save(session);

        List<AccessKey> activeKeys = accessKeyRepository.findBySessionSessionIdAndStatus(session.getSessionId(), "ACTIVE");
        for (AccessKey key : activeKeys) {
            key.setStatus("REVOKED");
            key.setRevokedAt(now);
        }
        if (!activeKeys.isEmpty()) {
            accessKeyRepository.saveAll(activeKeys);
        }

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(
                ActiveVisitorSessionDtos.CheckOutResponse.builder()
                    .data(
                        ActiveVisitorSessionDtos.CheckOutData.builder()
                            .checkoutId(checkout.getCheckoutId())
                            .sessionId(session.getSessionId())
                            .status(checkout.getStatus())
                            .checkoutAt(checkout.getCheckoutAt())
                            .access(
                                ActiveVisitorSessionDtos.AccessRevocation.builder()
                                    .temporaryKeysRevoked(true)
                                    .revokedAt(now)
                                    .build()
                            )
                            .build()
                    )
                    .build()
            );
    }

    @Transactional(readOnly = true)
    public ActiveVisitorSessionDtos.SessionDetailResponse getSessionDetail(UUID sessionId) {
        VisitSession session = visitSessionRepository.findById(sessionId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "SESSION_NOT_FOUND", "Session not found"));

        return ActiveVisitorSessionDtos.SessionDetailResponse.builder()
            .data(
                ActiveVisitorSessionDtos.SessionDetailData.builder()
                    .sessionId(session.getSessionId())
                    .status(session.getStatus())
                    .checkinAt(session.getCheckinAt())
                    .checkoutAt(session.getCheckoutAt())
                    .visitorName(session.getVisitor().getFullName())
                    .purposeLabel(session.getCheckIn().getPurpose().getLabel())
                    .build()
            )
            .build();
    }
}
