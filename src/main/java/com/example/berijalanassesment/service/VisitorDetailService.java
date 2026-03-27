package com.example.berijalanassesment.service;

import com.example.berijalanassesment.controller.support.ApiException;
import com.example.berijalanassesment.dto.screen.VisitorDetailViewDtos;
import com.example.berijalanassesment.models.CheckOut;
import com.example.berijalanassesment.models.DigitalPass;
import com.example.berijalanassesment.models.HostContact;
import com.example.berijalanassesment.models.HostNotification;
import com.example.berijalanassesment.models.PrintJob;
import com.example.berijalanassesment.models.RiskAnalysis;
import com.example.berijalanassesment.models.SessionFlag;
import com.example.berijalanassesment.models.SessionReport;
import com.example.berijalanassesment.models.User;
import com.example.berijalanassesment.models.VisitSession;
import com.example.berijalanassesment.repository.AccessKeyRepository;
import com.example.berijalanassesment.repository.CheckOutRepository;
import com.example.berijalanassesment.repository.DigitalPassRepository;
import com.example.berijalanassesment.repository.HostContactRepository;
import com.example.berijalanassesment.repository.HostNotificationRepository;
import com.example.berijalanassesment.repository.PrintJobRepository;
import com.example.berijalanassesment.repository.RiskAnalysisRepository;
import com.example.berijalanassesment.repository.SessionFlagRepository;
import com.example.berijalanassesment.repository.SessionReportRepository;
import com.example.berijalanassesment.repository.UserRepository;
import com.example.berijalanassesment.repository.VisitSessionRepository;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class VisitorDetailService {

    private final VisitSessionRepository visitSessionRepository;
    private final RiskAnalysisRepository riskAnalysisRepository;
    private final DigitalPassRepository digitalPassRepository;
    private final PrintJobRepository printJobRepository;
    private final HostNotificationRepository hostNotificationRepository;
    private final HostContactRepository hostContactRepository;
    private final CheckOutRepository checkOutRepository;
    private final AccessKeyRepository accessKeyRepository;
    private final SessionReportRepository sessionReportRepository;
    private final SessionFlagRepository sessionFlagRepository;
    private final UserRepository userRepository;
    private final RiskEngineService riskEngineService;

    public VisitorDetailService(
        VisitSessionRepository visitSessionRepository,
        RiskAnalysisRepository riskAnalysisRepository,
        DigitalPassRepository digitalPassRepository,
        PrintJobRepository printJobRepository,
        HostNotificationRepository hostNotificationRepository,
        HostContactRepository hostContactRepository,
        CheckOutRepository checkOutRepository,
        AccessKeyRepository accessKeyRepository,
        SessionReportRepository sessionReportRepository,
        SessionFlagRepository sessionFlagRepository,
        UserRepository userRepository,
        RiskEngineService riskEngineService
    ) {
        this.visitSessionRepository = visitSessionRepository;
        this.riskAnalysisRepository = riskAnalysisRepository;
        this.digitalPassRepository = digitalPassRepository;
        this.printJobRepository = printJobRepository;
        this.hostNotificationRepository = hostNotificationRepository;
        this.hostContactRepository = hostContactRepository;
        this.checkOutRepository = checkOutRepository;
        this.accessKeyRepository = accessKeyRepository;
        this.sessionReportRepository = sessionReportRepository;
        this.sessionFlagRepository = sessionFlagRepository;
        this.userRepository = userRepository;
        this.riskEngineService = riskEngineService;
    }

    @Transactional(readOnly = true)
    public VisitorDetailViewDtos.SessionDetailResponse getSessionDetail(UUID sessionId) {
        VisitSession session = getSessionOrThrow(sessionId);
        RiskAnalysis risk = riskAnalysisRepository.findBySessionSessionId(sessionId).orElse(null);

        int duration = (int) Duration.between(session.getCheckinAt(), session.getCheckoutAt() == null ? Instant.now() : session.getCheckoutAt()).toMinutes();

        return VisitorDetailViewDtos.SessionDetailResponse.builder()
            .data(
                VisitorDetailViewDtos.SessionDetailData.builder()
                    .sessionId(session.getSessionId())
                    .status(session.getStatus())
                    .visitor(
                        VisitorDetailViewDtos.SessionVisitor.builder()
                            .visitorId(session.getVisitor().getVisitorId())
                            .fullName(session.getVisitor().getFullName())
                            .avatarUrl(session.getCheckIn().getPhoto().getStorageUrl())
                            .identityLabel("Verified Guest")
                            .nik(session.getVisitor().getNik())
                            .kycStatus(session.getVisitor().getKycStatus())
                            .build()
                    )
                    .visit(
                        VisitorDetailViewDtos.SessionVisit.builder()
                            .purposeLabel(session.getCheckIn().getPurpose().getLabel())
                            .purposeSubtitle(buildPurposeSubtitle(session))
                            .checkinAt(OffsetDateTime.ofInstant(session.getCheckinAt(), ZoneId.systemDefault()))
                            .durationMinutes(duration)
                            .build()
                    )
                    .riskAnalysis(
                        VisitorDetailViewDtos.SessionRiskAnalysis.builder()
                            .riskLevel(risk == null ? "UNKNOWN" : risk.getRiskLevel())
                            .riskScore(risk == null ? null : risk.getRiskScore())
                            .summary(risk == null ? null : risk.getSummary())
                            .build()
                    )
                    .actions(
                        VisitorDetailViewDtos.SessionActions.builder()
                            .canPrintPass(true)
                            .canContactHost(true)
                            .canForceCheckout("ACTIVE".equalsIgnoreCase(session.getStatus()))
                            .canFlagSession(true)
                            .canDownloadReport(true)
                            .build()
                    )
                    .build()
            )
            .build();
    }

    public ResponseEntity<VisitorDetailViewDtos.PrintPassResponse> printPass(
        UUID sessionId,
        VisitorDetailViewDtos.PrintPassRequest request
    ) {
        VisitSession session = getSessionOrThrow(sessionId);
        DigitalPass pass = digitalPassRepository.findBySessionSessionId(sessionId)
            .orElseThrow(() -> new ApiException(HttpStatus.CONFLICT, "PASS_NOT_AVAILABLE", "Digital pass is not available"));

        Instant now = Instant.now();
        PrintJob job = printJobRepository.save(
            PrintJob.builder()
                .session(session)
                .pass(pass)
                .requestedByUser(resolveActorUser())
                .printerId(request.getPrinterId())
                .copies(request.getCopies())
                .status("QUEUED")
                .queuedAt(now)
                .completedAt(null)
                .build()
        );

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(
            VisitorDetailViewDtos.PrintPassResponse.builder()
                .data(
                    VisitorDetailViewDtos.PrintPassData.builder()
                        .jobId(job.getJobId())
                        .status(job.getStatus())
                        .queuedAt(OffsetDateTime.ofInstant(job.getQueuedAt(), ZoneId.systemDefault()))
                        .build()
                )
                .build()
        );
    }

    public VisitorDetailViewDtos.ContactHostResponse contactHost(
        UUID sessionId,
        VisitorDetailViewDtos.ContactHostRequest request
    ) {
        VisitSession session = getSessionOrThrow(sessionId);
        HostContact host = session.getHostContact();
        if (host == null) {
            host = hostContactRepository.findByIsActiveTrue().stream().findFirst()
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "HOST_CONTACT_NOT_FOUND", "Host contact not found"));
        }

        Instant now = Instant.now();
        HostNotification notification = hostNotificationRepository.save(
            HostNotification.builder()
                .session(session)
                .hostContact(host)
                .requestedByUser(resolveActorUser())
                .channel(request.getChannel())
                .message(request.getMessage())
                .status("SENT")
                .sentAt(now)
                .createdAt(now)
                .build()
        );

        return VisitorDetailViewDtos.ContactHostResponse.builder()
            .data(
                VisitorDetailViewDtos.ContactHostData.builder()
                    .notificationId(notification.getNotificationId())
                    .status(notification.getStatus())
                    .sentAt(OffsetDateTime.ofInstant(now, ZoneId.systemDefault()))
                    .build()
            )
            .build();
    }

    public ResponseEntity<VisitorDetailViewDtos.ForceCheckoutResponse> forceCheckout(
        UUID sessionId,
        VisitorDetailViewDtos.ForceCheckoutRequest request
    ) {
        VisitSession session = getSessionOrThrow(sessionId);

        if (checkOutRepository.existsBySessionSessionId(sessionId)) {
            throw new ApiException(HttpStatus.CONFLICT, "SESSION_ALREADY_CHECKED_OUT", "Session has already been checked out");
        }

        Instant now = Instant.now();
        CheckOut checkout = checkOutRepository.save(
            CheckOut.builder()
                .session(session)
                .performedByUser(resolveActorUser())
                .performedByDevice(null)
                .reasonCode(request.getReasonCode())
                .reasonNote(request.getReasonNote())
                .isForced(true)
                .status("FORCE_CHECKED_OUT")
                .checkoutAt(now)
                .createdAt(now)
                .build()
        );

        session.setStatus("FORCE_CHECKED_OUT");
        session.setCheckoutAt(now);
        session.setUpdatedAt(now);
        visitSessionRepository.save(session);

        accessKeyRepository.findBySessionSessionIdAndStatus(sessionId, "ACTIVE").forEach(key -> {
            key.setStatus("REVOKED");
            key.setRevokedAt(now);
        });

        riskEngineService.evaluateAndPersist(session, "FORCE_CHECKOUT", now);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(
                VisitorDetailViewDtos.ForceCheckoutResponse.builder()
                    .data(
                        VisitorDetailViewDtos.ForceCheckoutData.builder()
                            .checkoutId(checkout.getCheckoutId())
                            .sessionId(sessionId)
                            .status(checkout.getStatus())
                            .checkoutAt(OffsetDateTime.ofInstant(now, ZoneId.systemDefault()))
                            .accessRevoked(true)
                            .build()
                    )
                    .build()
            );
    }

    @Transactional(readOnly = true)
    public VisitorDetailViewDtos.DownloadReportResponse downloadSessionReport(UUID sessionId, String format) {
        getSessionOrThrow(sessionId);

        if (!"pdf".equalsIgnoreCase(format)) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "UNSUPPORTED_REPORT_FORMAT", "Only pdf format is supported");
        }

        SessionReport report = sessionReportRepository.findTopBySessionSessionIdAndFormatOrderByGeneratedAtDesc(sessionId, format)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "SESSION_NOT_FOUND", "Session report not found"));

        return VisitorDetailViewDtos.DownloadReportResponse.builder()
            .data(
                VisitorDetailViewDtos.DownloadReportData.builder()
                    .reportId(report.getReportId())
                    .format(report.getFormat())
                    .downloadUrl(report.getStorageUrl())
                    .expiresAt(report.getExpiresAt() == null ? null : OffsetDateTime.ofInstant(report.getExpiresAt(), ZoneId.systemDefault()))
                    .build()
            )
            .build();
    }

    public ResponseEntity<VisitorDetailViewDtos.FlagSessionResponse> flagSession(
        UUID sessionId,
        VisitorDetailViewDtos.FlagSessionRequest request
    ) {
        VisitSession session = getSessionOrThrow(sessionId);

        if (sessionFlagRepository.existsBySessionSessionIdAndStatus(sessionId, "OPEN")) {
            throw new ApiException(HttpStatus.CONFLICT, "FLAG_ALREADY_EXISTS", "An open flag already exists for this session");
        }

        Instant now = Instant.now();
        SessionFlag flag = sessionFlagRepository.save(
            SessionFlag.builder()
                .session(session)
                .createdByUser(resolveActorUser())
                .flagType(request.getFlagType())
                .note(request.getNote())
                .status("OPEN")
                .createdAt(now)
                .closedAt(null)
                .build()
        );

        riskEngineService.evaluateAndPersist(session, "SESSION_FLAGGED", now);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(
                VisitorDetailViewDtos.FlagSessionResponse.builder()
                    .data(
                        VisitorDetailViewDtos.FlagSessionData.builder()
                            .flagId(flag.getFlagId())
                            .sessionId(sessionId)
                            .status(flag.getStatus())
                            .createdAt(OffsetDateTime.ofInstant(flag.getCreatedAt(), ZoneId.systemDefault()))
                            .build()
                    )
                    .build()
            );
    }

    private VisitSession getSessionOrThrow(UUID sessionId) {
        return visitSessionRepository.findById(sessionId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "SESSION_NOT_FOUND", "Visitor session not found"));
    }

    private User resolveActorUser() {
        return userRepository.findByAccountStatus("ACTIVE").stream().findFirst()
            .orElseGet(() -> userRepository.save(
                User.builder()
                    .fullName("System User")
                    .email("system@vigi-gate.local")
                    .passwordHash("{noop}system")
                    .accountStatus("ACTIVE")
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .lastLoginAt(null)
                    .build()
            ));
    }

    private String buildPurposeSubtitle(VisitSession session) {
        HostContact hostContact = session.getHostContact();
        if (hostContact == null) {
            return "-";
        }

        String department = hostContact.getDepartment() == null ? "" : hostContact.getDepartment();
        String floor = hostContact.getFloorLabel() == null ? "" : hostContact.getFloorLabel();
        if (department.isBlank() && floor.isBlank()) {
            return "-";
        }
        if (department.isBlank()) {
            return floor;
        }
        if (floor.isBlank()) {
            return department;
        }
        return department + " - " + floor;
    }
}
