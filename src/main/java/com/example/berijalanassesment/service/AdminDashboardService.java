package com.example.berijalanassesment.service;

import com.example.berijalanassesment.controller.support.ApiException;
import com.example.berijalanassesment.dto.screen.AdminDashboardDtos;
import com.example.berijalanassesment.models.CheckIn;
import com.example.berijalanassesment.models.RiskAnalysis;
import com.example.berijalanassesment.models.RiskAlert;
import com.example.berijalanassesment.models.RiskAlertAction;
import com.example.berijalanassesment.models.User;
import com.example.berijalanassesment.models.VisitSession;
import com.example.berijalanassesment.models.Visitor;
import com.example.berijalanassesment.repository.CheckInRepository;
import com.example.berijalanassesment.repository.PrintJobRepository;
import com.example.berijalanassesment.repository.RiskAlertActionRepository;
import com.example.berijalanassesment.repository.RiskAlertRepository;
import com.example.berijalanassesment.repository.RiskAnalysisRepository;
import com.example.berijalanassesment.repository.UserRepository;
import com.example.berijalanassesment.repository.VisitSessionRepository;
import com.example.berijalanassesment.repository.VisitorRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminDashboardService {

    private final CheckInRepository checkInRepository;
    private final VisitSessionRepository visitSessionRepository;
    private final RiskAlertRepository riskAlertRepository;
    private final PrintJobRepository printJobRepository;
    private final VisitorRepository visitorRepository;
    private final RiskAlertActionRepository riskAlertActionRepository;
    private final UserRepository userRepository;
    private final RiskAnalysisRepository riskAnalysisRepository;

    public AdminDashboardService(
        CheckInRepository checkInRepository,
        VisitSessionRepository visitSessionRepository,
        RiskAlertRepository riskAlertRepository,
        PrintJobRepository printJobRepository,
        VisitorRepository visitorRepository,
        RiskAlertActionRepository riskAlertActionRepository,
        UserRepository userRepository,
        RiskAnalysisRepository riskAnalysisRepository
    ) {
        this.checkInRepository = checkInRepository;
        this.visitSessionRepository = visitSessionRepository;
        this.riskAlertRepository = riskAlertRepository;
        this.printJobRepository = printJobRepository;
        this.visitorRepository = visitorRepository;
        this.riskAlertActionRepository = riskAlertActionRepository;
        this.userRepository = userRepository;
        this.riskAnalysisRepository = riskAnalysisRepository;
    }

    @Transactional(readOnly = true)
    public AdminDashboardDtos.DashboardSummaryResponse getSummary(String date, String timezone) {
        LocalDate requestedDate = LocalDate.parse(date);
        ZoneId zoneId = ZoneId.of(timezone);

        Instant start = requestedDate.atStartOfDay(zoneId).toInstant();
        Instant end = requestedDate.plusDays(1).atStartOfDay(zoneId).toInstant();

        long totalVisitorsToday = checkInRepository.findAll().stream()
            .filter(checkIn -> checkIn.getCheckinAt() != null)
            .filter(checkIn -> checkIn.getCheckinAt().isAfter(start) || checkIn.getCheckinAt().equals(start))
            .filter(checkIn -> checkIn.getCheckinAt().isBefore(end))
            .count();

        long currentlyActive = visitSessionRepository.findAll().stream()
            .filter(session -> "ACTIVE".equalsIgnoreCase(session.getStatus()))
            .count();

        long highRiskVisitors = riskAnalysisRepository.countByRiskLevelAndSessionStatus("HIGH", "ACTIVE");
        long badgeQueueCount = printJobRepository.countByStatus("QUEUED");

        return AdminDashboardDtos.DashboardSummaryResponse.builder()
            .data(
                AdminDashboardDtos.DashboardSummaryData.builder()
                    .totalVisitorsToday((int) totalVisitorsToday)
                    .currentlyActive((int) currentlyActive)
                    .highRiskVisitors((int) highRiskVisitors)
                    .badgeQueueCount((int) badgeQueueCount)
                    .generatedAt(OffsetDateTime.now(zoneId))
                    .build()
            )
            .build();
    }

    @Transactional(readOnly = true)
    public AdminDashboardDtos.ListVisitorsResponse listVisitors(
        String query,
        String status,
        String riskLevel,
        String timeframe,
        int page,
        int pageSize
    ) {
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), pageSize);
        ZoneId zoneId = ZoneId.of("Asia/Jakarta");
        String normalizedStatus = status == null ? "ALL" : status.trim().toUpperCase(Locale.ROOT);
        String normalizedRisk = riskLevel == null ? "ALL" : riskLevel.trim().toUpperCase(Locale.ROOT);
        Page<VisitSession> sessions;
        if ("ALL".equalsIgnoreCase(timeframe)) {
            sessions = visitSessionRepository.searchLatestSessions(
                query,
                normalizedStatus,
                normalizedRisk,
                pageable
            );
        } else {
            LocalDate today = LocalDate.now(zoneId);
            Instant startAt = today.atStartOfDay(zoneId).toInstant();
            Instant endAt = today.plusDays(1).atStartOfDay(zoneId).toInstant();
            sessions = visitSessionRepository.searchLatestSessionsInRange(
                query,
                normalizedStatus,
                normalizedRisk,
                startAt,
                endAt,
                pageable
            );
        }

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm").withZone(zoneId);

        List<AdminDashboardDtos.VisitorRow> rows = sessions.stream().map(session -> {
            Visitor visitor = session.getVisitor();
            CheckIn checkIn = session.getCheckIn();
            RiskAnalysis analysis = "ACTIVE".equalsIgnoreCase(session.getStatus())
                ? riskAnalysisRepository.findBySessionSessionId(session.getSessionId()).orElse(null)
                : null;
            String computedRisk = analysis == null ? "UNKNOWN" : analysis.getRiskLevel();

            return AdminDashboardDtos.VisitorRow.builder()
                .sessionId(session.getSessionId())
                .visitorId(visitor.getVisitorId())
                .name(visitor.getFullName())
                .email(visitor.getEmail())
                .nik(visitor.getNik())
                .purposeLabel(checkIn == null || checkIn.getPurpose() == null ? "-" : checkIn.getPurpose().getLabel())
                .checkinTime(checkIn == null || checkIn.getCheckinAt() == null ? "-" : timeFormatter.format(checkIn.getCheckinAt()))
                .status(session.getStatus())
                .riskLevel(computedRisk)
                .build();
        }).toList();

        return AdminDashboardDtos.ListVisitorsResponse.builder()
            .data(rows)
            .meta(
                AdminDashboardDtos.VisitorListMeta.builder()
                    .page(page)
                    .pageSize(pageSize)
                    .totalRecords((int) sessions.getTotalElements())
                    .totalPages(sessions.getTotalPages())
                    .build()
            )
            .build();
    }

    @Transactional(readOnly = true)
    public AdminDashboardDtos.RiskAlertsResponse getRiskAlerts(String status, int limit) {
        Pageable pageable = PageRequest.of(0, Math.max(limit, 1));
        Page<RiskAlert> alerts = riskAlertRepository.findByStatusOrderByCreatedAtDesc(status, pageable);

        return AdminDashboardDtos.RiskAlertsResponse.builder()
            .data(
                alerts.stream()
                    .map(alert -> AdminDashboardDtos.RiskAlertItem.builder()
                        .alertId(alert.getAlertId())
                        .severity(alert.getSeverity())
                        .title(alert.getTitle())
                        .description(alert.getDescription())
                        .createdAt(OffsetDateTime.ofInstant(alert.getCreatedAt(), ZoneId.systemDefault()))
                        .relativeTime("just now")
                        .status(alert.getStatus())
                        .build())
                    .toList()
            )
            .build();
    }

    public AdminDashboardDtos.RiskAlertActionResponse handleRiskAlertAction(
        UUID alertId,
        AdminDashboardDtos.RiskAlertActionRequest request
    ) {
        RiskAlert alert = riskAlertRepository.findById(alertId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ALERT_NOT_FOUND", "Alert not found"));

        if ("DISMISSED".equalsIgnoreCase(alert.getStatus()) || "RESOLVED".equalsIgnoreCase(alert.getStatus())) {
            throw new ApiException(HttpStatus.CONFLICT, "ALERT_ALREADY_CLOSED", "Alert is already closed");
        }

        User actedByUser = resolveActorUser();
        Instant now = Instant.now();

        riskAlertActionRepository.save(
            RiskAlertAction.builder()
                .alert(alert)
                .actedByUser(actedByUser)
                .action(request.getAction())
                .note(request.getNote())
                .actedAt(now)
                .build()
        );

        String newStatus = "DISMISS".equalsIgnoreCase(request.getAction()) ? "DISMISSED" : "RESOLVED";
        alert.setStatus(newStatus);
        alert.setClosedAt(now);
        riskAlertRepository.save(alert);

        return AdminDashboardDtos.RiskAlertActionResponse.builder()
            .data(
                AdminDashboardDtos.RiskAlertActionData.builder()
                    .alertId(alert.getAlertId())
                    .status(alert.getStatus())
                    .actedBy(actedByUser.getEmail())
                    .actedAt(OffsetDateTime.ofInstant(now, ZoneId.systemDefault()))
                    .build()
            )
            .build();
    }

    public ResponseEntity<AdminDashboardDtos.QuickRegisterVisitorResponse> quickRegister(
        AdminDashboardDtos.QuickRegisterVisitorRequest request
    ) {
        throw new ApiException(HttpStatus.NOT_IMPLEMENTED, "USE_CHECKINS_ENDPOINT", "Use POST /api/v1/checkins for quick register flow");
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
}
