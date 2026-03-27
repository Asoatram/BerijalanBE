package com.example.berijalanassesment.seed;

import com.example.berijalanassesment.models.AccessKey;
import com.example.berijalanassesment.models.CheckIn;
import com.example.berijalanassesment.models.CheckOut;
import com.example.berijalanassesment.models.DailyReport;
import com.example.berijalanassesment.models.DailyReportAnomaly;
import com.example.berijalanassesment.models.DailyReportDataSource;
import com.example.berijalanassesment.models.DailyReportRecommendation;
import com.example.berijalanassesment.models.DailyReportSummary;
import com.example.berijalanassesment.models.Device;
import com.example.berijalanassesment.models.DigitalPass;
import com.example.berijalanassesment.models.HostContact;
import com.example.berijalanassesment.models.HostNotification;
import com.example.berijalanassesment.models.Permission;
import com.example.berijalanassesment.models.PortraitMedia;
import com.example.berijalanassesment.models.PrintJob;
import com.example.berijalanassesment.models.ReportExport;
import com.example.berijalanassesment.models.ReportGenerationJob;
import com.example.berijalanassesment.models.RiskAlert;
import com.example.berijalanassesment.models.RiskAlertAction;
import com.example.berijalanassesment.models.RiskAnalysis;
import com.example.berijalanassesment.models.RiskSignal;
import com.example.berijalanassesment.models.Role;
import com.example.berijalanassesment.models.RolePermission;
import com.example.berijalanassesment.models.SessionFlag;
import com.example.berijalanassesment.models.SessionReport;
import com.example.berijalanassesment.models.User;
import com.example.berijalanassesment.models.UserRole;
import com.example.berijalanassesment.models.VisitIntent;
import com.example.berijalanassesment.models.VisitSession;
import com.example.berijalanassesment.models.Visitor;
import com.example.berijalanassesment.repository.AccessKeyRepository;
import com.example.berijalanassesment.repository.AuthRefreshTokenRepository;
import com.example.berijalanassesment.repository.CheckInRepository;
import com.example.berijalanassesment.repository.CheckOutRepository;
import com.example.berijalanassesment.repository.DailyReportAnomalyRepository;
import com.example.berijalanassesment.repository.DailyReportDataSourceRepository;
import com.example.berijalanassesment.repository.DailyReportRecommendationRepository;
import com.example.berijalanassesment.repository.DailyReportRepository;
import com.example.berijalanassesment.repository.DailyReportSummaryRepository;
import com.example.berijalanassesment.repository.DeviceRepository;
import com.example.berijalanassesment.repository.DigitalPassRepository;
import com.example.berijalanassesment.repository.HostContactRepository;
import com.example.berijalanassesment.repository.HostNotificationRepository;
import com.example.berijalanassesment.repository.PermissionRepository;
import com.example.berijalanassesment.repository.PortraitMediaRepository;
import com.example.berijalanassesment.repository.PrintJobRepository;
import com.example.berijalanassesment.repository.ReportExportRepository;
import com.example.berijalanassesment.repository.ReportGenerationJobRepository;
import com.example.berijalanassesment.repository.RiskAlertActionRepository;
import com.example.berijalanassesment.repository.RiskAlertRepository;
import com.example.berijalanassesment.repository.RiskAnalysisRepository;
import com.example.berijalanassesment.repository.RiskSignalRepository;
import com.example.berijalanassesment.repository.RolePermissionRepository;
import com.example.berijalanassesment.repository.RoleRepository;
import com.example.berijalanassesment.repository.SessionFlagRepository;
import com.example.berijalanassesment.repository.SessionReportRepository;
import com.example.berijalanassesment.repository.UserRepository;
import com.example.berijalanassesment.repository.UserRoleRepository;
import com.example.berijalanassesment.repository.VisitIntentRepository;
import com.example.berijalanassesment.repository.VisitSessionRepository;
import com.example.berijalanassesment.repository.VisitorRepository;
import com.example.berijalanassesment.security.AuthPermissions;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;

@Component
@Profile("dev")
@Order(10)
@EnableConfigurationProperties(AppSeedProperties.class)
public class DevDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DevDataSeeder.class);

    private final AppSeedProperties properties;
    private final PasswordEncoder passwordEncoder;

    private final AccessKeyRepository accessKeyRepository;
    private final AuthRefreshTokenRepository authRefreshTokenRepository;
    private final CheckInRepository checkInRepository;
    private final CheckOutRepository checkOutRepository;
    private final DailyReportAnomalyRepository dailyReportAnomalyRepository;
    private final DailyReportDataSourceRepository dailyReportDataSourceRepository;
    private final DailyReportRecommendationRepository dailyReportRecommendationRepository;
    private final DailyReportRepository dailyReportRepository;
    private final DailyReportSummaryRepository dailyReportSummaryRepository;
    private final DeviceRepository deviceRepository;
    private final DigitalPassRepository digitalPassRepository;
    private final HostContactRepository hostContactRepository;
    private final HostNotificationRepository hostNotificationRepository;
    private final PermissionRepository permissionRepository;
    private final PortraitMediaRepository portraitMediaRepository;
    private final PrintJobRepository printJobRepository;
    private final ReportExportRepository reportExportRepository;
    private final ReportGenerationJobRepository reportGenerationJobRepository;
    private final RiskAlertActionRepository riskAlertActionRepository;
    private final RiskAlertRepository riskAlertRepository;
    private final RiskAnalysisRepository riskAnalysisRepository;
    private final RiskSignalRepository riskSignalRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final RoleRepository roleRepository;
    private final SessionFlagRepository sessionFlagRepository;
    private final SessionReportRepository sessionReportRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final VisitIntentRepository visitIntentRepository;
    private final VisitSessionRepository visitSessionRepository;
    private final VisitorRepository visitorRepository;

    public DevDataSeeder(
        AppSeedProperties properties,
        PasswordEncoder passwordEncoder,
        AccessKeyRepository accessKeyRepository,
        AuthRefreshTokenRepository authRefreshTokenRepository,
        CheckInRepository checkInRepository,
        CheckOutRepository checkOutRepository,
        DailyReportAnomalyRepository dailyReportAnomalyRepository,
        DailyReportDataSourceRepository dailyReportDataSourceRepository,
        DailyReportRecommendationRepository dailyReportRecommendationRepository,
        DailyReportRepository dailyReportRepository,
        DailyReportSummaryRepository dailyReportSummaryRepository,
        DeviceRepository deviceRepository,
        DigitalPassRepository digitalPassRepository,
        HostContactRepository hostContactRepository,
        HostNotificationRepository hostNotificationRepository,
        PermissionRepository permissionRepository,
        PortraitMediaRepository portraitMediaRepository,
        PrintJobRepository printJobRepository,
        ReportExportRepository reportExportRepository,
        ReportGenerationJobRepository reportGenerationJobRepository,
        RiskAlertActionRepository riskAlertActionRepository,
        RiskAlertRepository riskAlertRepository,
        RiskAnalysisRepository riskAnalysisRepository,
        RiskSignalRepository riskSignalRepository,
        RolePermissionRepository rolePermissionRepository,
        RoleRepository roleRepository,
        SessionFlagRepository sessionFlagRepository,
        SessionReportRepository sessionReportRepository,
        UserRepository userRepository,
        UserRoleRepository userRoleRepository,
        VisitIntentRepository visitIntentRepository,
        VisitSessionRepository visitSessionRepository,
        VisitorRepository visitorRepository
    ) {
        this.properties = properties;
        this.passwordEncoder = passwordEncoder;
        this.accessKeyRepository = accessKeyRepository;
        this.authRefreshTokenRepository = authRefreshTokenRepository;
        this.checkInRepository = checkInRepository;
        this.checkOutRepository = checkOutRepository;
        this.dailyReportAnomalyRepository = dailyReportAnomalyRepository;
        this.dailyReportDataSourceRepository = dailyReportDataSourceRepository;
        this.dailyReportRecommendationRepository = dailyReportRecommendationRepository;
        this.dailyReportRepository = dailyReportRepository;
        this.dailyReportSummaryRepository = dailyReportSummaryRepository;
        this.deviceRepository = deviceRepository;
        this.digitalPassRepository = digitalPassRepository;
        this.hostContactRepository = hostContactRepository;
        this.hostNotificationRepository = hostNotificationRepository;
        this.permissionRepository = permissionRepository;
        this.portraitMediaRepository = portraitMediaRepository;
        this.printJobRepository = printJobRepository;
        this.reportExportRepository = reportExportRepository;
        this.reportGenerationJobRepository = reportGenerationJobRepository;
        this.riskAlertActionRepository = riskAlertActionRepository;
        this.riskAlertRepository = riskAlertRepository;
        this.riskAnalysisRepository = riskAnalysisRepository;
        this.riskSignalRepository = riskSignalRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.roleRepository = roleRepository;
        this.sessionFlagRepository = sessionFlagRepository;
        this.sessionReportRepository = sessionReportRepository;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.visitIntentRepository = visitIntentRepository;
        this.visitSessionRepository = visitSessionRepository;
        this.visitorRepository = visitorRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!properties.isEnabled()) {
            log.info("DevDataSeeder skipped: app.seed.enabled=false");
            return;
        }

        if (properties.isWipe()) {
            wipeData();
        }
        seedData();

        log.info("DevDataSeeder completed. visitors={}, sessions={}, reports={}",
            visitorRepository.count(),
            visitSessionRepository.count(),
            dailyReportRepository.count());
    }

    private void wipeData() {
        riskSignalRepository.deleteAllInBatch();
        riskAlertActionRepository.deleteAllInBatch();
        sessionFlagRepository.deleteAllInBatch();
        hostNotificationRepository.deleteAllInBatch();
        printJobRepository.deleteAllInBatch();
        sessionReportRepository.deleteAllInBatch();
        accessKeyRepository.deleteAllInBatch();
        checkOutRepository.deleteAllInBatch();
        digitalPassRepository.deleteAllInBatch();
        riskAlertRepository.deleteAllInBatch();
        riskAnalysisRepository.deleteAllInBatch();
        visitSessionRepository.deleteAllInBatch();
        checkInRepository.deleteAllInBatch();
        portraitMediaRepository.deleteAllInBatch();

        reportExportRepository.deleteAllInBatch();
        reportGenerationJobRepository.deleteAllInBatch();
        dailyReportDataSourceRepository.deleteAllInBatch();
        dailyReportRecommendationRepository.deleteAllInBatch();
        dailyReportAnomalyRepository.deleteAllInBatch();
        dailyReportSummaryRepository.deleteAllInBatch();
        dailyReportRepository.deleteAllInBatch();

        visitorRepository.deleteAllInBatch();
        hostContactRepository.deleteAllInBatch();
        authRefreshTokenRepository.deleteAllInBatch();
        userRoleRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();

        visitIntentRepository.deleteAllInBatch();
        deviceRepository.deleteAllInBatch();

        log.info("DevDataSeeder wiped existing data");
    }

    private void seedData() {
        Instant now = Instant.now();
        ZoneId jakarta = ZoneId.of("Asia/Jakarta");
        LocalDate todayJakarta = LocalDate.now(jakarta);

        Role adminRole = ensureRole("ADMIN", "Administrator");
        Role securityRole = ensureRole("SECURITY", "Security Operator");
        ensurePermissionsAttached(adminRole);
        ensurePermissionsAttached(securityRole);

        User admin = ensureUser("VigiGate Admin", "admin@vigi-gate.com", "Admin123!", now);
        User security = ensureUser("Security Operator", "security@vigi-gate.com", "Admin123!", now);

        ensureUserRole(admin, adminRole, now);
        ensureUserRole(security, securityRole, now);

        Device kiosk = saveDevice("kiosk-01", "KIOSK", "Main Entrance Kiosk", now);
        Device adminDashboard = saveDevice("admin-dashboard-01", "ADMIN_DASHBOARD", "Security Control Room", now);
        Device securityDesk = saveDevice("security-desk-01", "SECURITY_DESK", "Main Security Desk", now);
        saveDevice("gate-station-a", "GATE_STATION", "Gate A", now);

        VisitIntent intentMaintenance = saveVisitIntent("IT_INFRA_MAINTENANCE", "IT Infrastructure Maintenance", 1);
        VisitIntent intentServerRoom = saveVisitIntent("SERVER_ROOM_UNSCHEDULED", "Server Room Access (Unscheduled)", 2);
        VisitIntent intentVendorMeeting = saveVisitIntent("VENDOR_PARTNERSHIP", "Vendor Partnership Meeting", 3);
        VisitIntent intentMeeting = saveVisitIntent("SCHEDULED_MEETING", "Scheduled Meeting", 4);
        VisitIntent intentDelivery = saveVisitIntent("PACKAGE_DELIVERY", "Package Delivery", 5);

        HostContact financeHost = saveHostContact(
            "Ratna Permata",
            "Finance Dept",
            "4th Floor",
            "+62-21-555-1400",
            "finance.host@vigi-gate.com"
        );
        HostContact itHost = saveHostContact(
            "Bima Nugroho",
            "IT Operations",
            "2nd Floor",
            "+62-21-555-2200",
            "it.host@vigi-gate.com"
        );

        Visitor ahmad = saveVisitor("Ahmad Sudirman", "3201234567890001", "ahmad.s@email.com", "COMPLIANT", now);
        Visitor john = saveVisitor("John Doe", "9982736450192837", "j.doe@contractor.net", "REVIEW_REQUIRED", now);
        Visitor siti = saveVisitor("Siti Aminah", "3174032211000001", "siti.aminah@global.id", "COMPLIANT", now);
        Visitor robert = saveVisitor("Robert Kurniawan", "3578010512185004", "robert.k@delivery.co", "COMPLIANT", now);

        PortraitMedia ahmadPhoto = savePortrait(
            kiosk,
            "https://cdn.vigi-gate.com/portraits/ahmad.jpg",
            "1111111111111111111111111111111111111111111111111111111111111111",
            BigDecimal.valueOf(0.95),
            now.minusSeconds(80 * 60)
        );
        PortraitMedia johnPhoto = savePortrait(
            kiosk,
            "https://cdn.vigi-gate.com/portraits/john.jpg",
            "2222222222222222222222222222222222222222222222222222222222222222",
            BigDecimal.valueOf(0.42),
            now.minusSeconds(60 * 60)
        );
        PortraitMedia sitiPhoto = savePortrait(
            kiosk,
            "https://cdn.vigi-gate.com/portraits/siti.jpg",
            "3333333333333333333333333333333333333333333333333333333333333333",
            BigDecimal.valueOf(0.90),
            now.minusSeconds(40 * 60)
        );
        PortraitMedia robertPhoto = savePortrait(
            kiosk,
            "https://cdn.vigi-gate.com/portraits/robert.jpg",
            "4444444444444444444444444444444444444444444444444444444444444444",
            BigDecimal.valueOf(0.88),
            now.minusSeconds(210 * 60)
        );

        CheckIn ahmadCheckIn = saveCheckIn(ahmad, intentMaintenance, ahmadPhoto, adminDashboard, now.minusSeconds(75 * 60));
        CheckIn johnCheckIn = saveCheckIn(john, intentServerRoom, johnPhoto, adminDashboard, now.minusSeconds(48 * 60));
        CheckIn sitiCheckIn = saveCheckIn(siti, intentVendorMeeting, sitiPhoto, adminDashboard, now.minusSeconds(35 * 60));
        CheckIn robertCheckIn = saveCheckIn(robert, intentDelivery, robertPhoto, securityDesk, now.minusSeconds(190 * 60));

        VisitSession ahmadSession = saveSession(ahmadCheckIn, ahmad, financeHost, "ACTIVE", ahmadCheckIn.getCheckinAt(), null, now);
        VisitSession johnSession = saveSession(johnCheckIn, john, itHost, "ACTIVE", johnCheckIn.getCheckinAt(), null, now);
        VisitSession sitiSession = saveSession(sitiCheckIn, siti, financeHost, "ACTIVE", sitiCheckIn.getCheckinAt(), null, now);
        VisitSession robertSession = saveSession(
            robertCheckIn,
            robert,
            null,
            "CHECKED_OUT",
            robertCheckIn.getCheckinAt(),
            now.minusSeconds(45 * 60),
            now
        );

        DigitalPass ahmadPass = saveDigitalPass(ahmadSession, "VG-PASS-0001", "ISSUED", now.minusSeconds(70 * 60), null);
        DigitalPass johnPass = saveDigitalPass(johnSession, "VG-PASS-0002", "ISSUED", now.minusSeconds(45 * 60), null);
        DigitalPass sitiPass = saveDigitalPass(sitiSession, "VG-PASS-0003", "ISSUED", now.minusSeconds(32 * 60), null);
        saveDigitalPass(robertSession, "VG-PASS-0004", "REVOKED", now.minusSeconds(180 * 60), now.minusSeconds(45 * 60));

        saveAccessKey(ahmadSession, "TEMP_BADGE", "ACTIVE", now.minusSeconds(70 * 60), null);
        saveAccessKey(johnSession, "TEMP_BADGE", "ACTIVE", now.minusSeconds(45 * 60), null);
        saveAccessKey(sitiSession, "TEMP_BADGE", "ACTIVE", now.minusSeconds(32 * 60), null);
        saveAccessKey(robertSession, "TEMP_BADGE", "REVOKED", now.minusSeconds(180 * 60), now.minusSeconds(45 * 60));

        checkOutRepository.save(
            CheckOut.builder()
                .session(robertSession)
                .performedByUser(security)
                .performedByDevice(securityDesk)
                .reasonCode("VISIT_COMPLETED")
                .reasonNote("Routine checkout")
                .isForced(false)
                .status("CHECKED_OUT")
                .checkoutAt(now.minusSeconds(45 * 60))
                .createdAt(now.minusSeconds(45 * 60))
                .build()
        );

        RiskAnalysis ahmadRisk = saveRiskAnalysis(ahmadSession, "LOW", 12,
            "Regular visitor, matches pre-registered profile. No anomalies detected.", now.minusSeconds(74 * 60));
        RiskAnalysis johnRisk = saveRiskAnalysis(johnSession, "HIGH", 87,
            "Multiple elevated indicators: low portrait quality and unscheduled sensitive-area access.", now.minusSeconds(47 * 60));
        RiskAnalysis sitiRisk = saveRiskAnalysis(sitiSession, "LOW", 20,
            "Low risk profile with standard business-hours access pattern.", now.minusSeconds(34 * 60));
        RiskAnalysis robertRisk = saveRiskAnalysis(robertSession, "MEDIUM", 48,
            "Moderate risk due to repeated delivery windows and previous alert history.", now.minusSeconds(175 * 60));

        saveRiskSignals(ahmadRisk, now.minusSeconds(74 * 60), List.of(
            signal("KYC_COMPLIANT", 0, "kyc_status=COMPLIANT"),
            signal("PHOTO_QUALITY", 0, "quality_score=0.95"),
            signal("VISIT_PURPOSE", 10, "purpose=it_infra_maintenance")
        ));
        saveRiskSignals(johnRisk, now.minusSeconds(47 * 60), List.of(
            signal("KYC_REVIEW_REQUIRED", 25, "kyc_status=REVIEW_REQUIRED"),
            signal("PHOTO_QUALITY", 35, "quality_score=0.42"),
            signal("VISIT_PURPOSE", 25, "purpose=server_room_unscheduled")
        ));
        saveRiskSignals(sitiRisk, now.minusSeconds(34 * 60), List.of(
            signal("KYC_COMPLIANT", 0, "kyc_status=COMPLIANT"),
            signal("PHOTO_QUALITY", 0, "quality_score=0.90"),
            signal("VISIT_PURPOSE", 15, "purpose=vendor_partnership")
        ));
        saveRiskSignals(robertRisk, now.minusSeconds(175 * 60), List.of(
            signal("KYC_COMPLIANT", 0, "kyc_status=COMPLIANT"),
            signal("VISIT_PURPOSE", 15, "purpose=package_delivery"),
            signal("FREQUENT_CHECKIN_7D", 15, "count_7d=6")
        ));

        RiskAlert openHighAlert = riskAlertRepository.save(
            RiskAlert.builder()
                .visitor(john)
                .session(johnSession)
                .severity("HIGH")
                .title("Unrecognized NIK detected")
                .description("South Wing")
                .status("OPEN")
                .createdAt(now.minusSeconds(2 * 60))
                .closedAt(null)
                .build()
        );

        RiskAlert resolvedAlert = riskAlertRepository.save(
            RiskAlert.builder()
                .visitor(robert)
                .session(robertSession)
                .severity("MEDIUM")
                .title("Expired temporary credential")
                .description("Gate B")
                .status("RESOLVED")
                .createdAt(now.minusSeconds(8 * 60 * 60))
                .closedAt(now.minusSeconds(7 * 60 * 60))
                .build()
        );

        riskAlertActionRepository.save(
            RiskAlertAction.builder()
                .alert(resolvedAlert)
                .actedByUser(security)
                .action("RESOLVE")
                .note("Credential verified and replaced")
                .actedAt(now.minusSeconds(7 * 60 * 60))
                .build()
        );

        sessionReportRepository.save(
            SessionReport.builder()
                .session(ahmadSession)
                .format("pdf")
                .storageUrl("https://cdn.vigi-gate.com/reports/session-" + ahmadSession.getSessionId() + ".pdf")
                .expiresAt(now.plusSeconds(2 * 60 * 60))
                .generatedAt(now.minusSeconds(10 * 60))
                .generatedByUser(admin)
                .build()
        );

        sessionFlagRepository.save(
            SessionFlag.builder()
                .session(robertSession)
                .createdByUser(security)
                .flagType("DOCUMENT_MISMATCH")
                .note("Resolved after manual verification")
                .status("CLOSED")
                .createdAt(now.minusSeconds(9 * 60 * 60))
                .closedAt(now.minusSeconds(8 * 60 * 60))
                .build()
        );

        hostNotificationRepository.save(
            HostNotification.builder()
                .session(ahmadSession)
                .hostContact(financeHost)
                .requestedByUser(security)
                .channel("WHATSAPP")
                .message("Your guest Ahmad Sudirman has arrived at the security desk.")
                .status("SENT")
                .sentAt(now.minusSeconds(60 * 60))
                .createdAt(now.minusSeconds(60 * 60))
                .build()
        );

        for (int i = 1; i <= 5; i++) {
            printJobRepository.save(
                PrintJob.builder()
                    .session(i % 2 == 0 ? johnSession : ahmadSession)
                    .pass(i % 2 == 0 ? johnPass : ahmadPass)
                    .requestedByUser(security)
                    .printerId("PRINTER-0" + i)
                    .copies(1)
                    .status("QUEUED")
                    .queuedAt(now.minusSeconds(i * 60L))
                    .completedAt(null)
                    .build()
            );
        }

        printJobRepository.save(
            PrintJob.builder()
                .session(sitiSession)
                .pass(sitiPass)
                .requestedByUser(admin)
                .printerId("PRINTER-06")
                .copies(1)
                .status("COMPLETED")
                .queuedAt(now.minusSeconds(30 * 60))
                .completedAt(now.minusSeconds(25 * 60))
                .build()
        );

        DailyReport dailyReport = dailyReportRepository.save(
            DailyReport.builder()
                .reportDate(todayJakarta)
                .timezone("Asia/Jakarta")
                .title("Daily Security Intelligence")
                .subtitle("Comprehensive insights for " + todayJakarta)
                .author("Sentinal Core AI v4.2")
                .confidenceScore(BigDecimal.valueOf(98.4))
                .lastSyncAt(now.minusSeconds(2 * 60))
                .generatedAt(now.minusSeconds(2 * 60))
                .status("READY")
                .build()
        );

        dailyReportSummaryRepository.save(
            DailyReportSummary.builder()
                .dailyReport(dailyReport)
                .totalVisitors(42)
                .totalVisitorsChangePct(BigDecimal.valueOf(12))
                .peakTrafficWindow("10 AM - 12 PM")
                .peakWindowSharePct(BigDecimal.valueOf(42))
                .alertsTriggered(3)
                .alertsResolutionStatus("All Resolved")
                .dailyPatterns(
                    "Visitor flow was steady through late morning, with routine check-ins dominating the queue. "
                        + "Operational throughput stayed consistent and all flagged events were closed within the same shift."
                )
                .build()
        );

        dailyReportAnomalyRepository.save(
            DailyReportAnomaly.builder()
                .dailyReport(dailyReport)
                .severity("MEDIUM")
                .description("Two contractor entries required manual roster confirmation at the loading dock due to delayed vendor sync.")
                .sortOrder(1)
                .build()
        );
        dailyReportAnomalyRepository.save(
            DailyReportAnomaly.builder()
                .dailyReport(dailyReport)
                .severity("LOW")
                .description("Brief queue spillover occurred near the visitor lane during lunch handover, adding minor screening delays.")
                .sortOrder(2)
                .build()
        );

        dailyReportRecommendationRepository.save(
            DailyReportRecommendation.builder()
                .dailyReport(dailyReport)
                .recommendationText("Update the contractor credential database before 06:00 AM.")
                .sortOrder(1)
                .build()
        );
        dailyReportRecommendationRepository.save(
            DailyReportRecommendation.builder()
                .dailyReport(dailyReport)
                .recommendationText("Add one floating screener for the lunch handover window to absorb short queue spikes.")
                .sortOrder(2)
                .build()
        );
        dailyReportRecommendationRepository.save(
            DailyReportRecommendation.builder()
                .dailyReport(dailyReport)
                .recommendationText("Pilot a secondary QR checkpoint for high-risk vendor zones.")
                .sortOrder(3)
                .build()
        );

        dailyReportDataSourceRepository.save(
            DailyReportDataSource.builder()
                .dailyReport(dailyReport)
                .sourceName("Gates A")
                .sortOrder(1)
                .build()
        );
        dailyReportDataSourceRepository.save(
            DailyReportDataSource.builder()
                .dailyReport(dailyReport)
                .sourceName("Gates B")
                .sortOrder(2)
                .build()
        );
        dailyReportDataSourceRepository.save(
            DailyReportDataSource.builder()
                .dailyReport(dailyReport)
                .sourceName("Gates C")
                .sortOrder(3)
                .build()
        );
        dailyReportDataSourceRepository.save(
            DailyReportDataSource.builder()
                .dailyReport(dailyReport)
                .sourceName("Gates D")
                .sortOrder(4)
                .build()
        );

        reportGenerationJobRepository.save(
            ReportGenerationJob.builder()
                .reportDate(todayJakarta)
                .timezone("Asia/Jakarta")
                .forceRegenerate(false)
                .status("COMPLETED")
                .requestedByUser(admin)
                .queuedAt(now.minusSeconds(3 * 60))
                .startedAt(now.minusSeconds(3 * 60))
                .completedAt(now.minusSeconds(2 * 60))
                .dailyReport(dailyReport)
                .build()
        );

        reportExportRepository.save(
            ReportExport.builder()
                .dailyReport(dailyReport)
                .exportedByUser(admin)
                .format("pdf")
                .includeSectionsJson("[\"overview\",\"intelligence\",\"metadata\"]")
                .downloadUrl("https://cdn.vigi-gate.com/reports/" + dailyReport.getDailyReportId() + ".pdf")
                .expiresAt(now.plusSeconds(2 * 60 * 60))
                .createdAt(now.minusSeconds(60))
                .build()
        );

        log.info("DevDataSeeder inserted fixture graph. openAlertId={}", openHighAlert.getAlertId());
        log.info("Demo login ready: email=admin@vigi-gate.com, password=Admin123!");
        log.info("Demo seed includes intents: {}, {}, {}, {}, {}",
            intentMaintenance.getCode(),
            intentServerRoom.getCode(),
            intentVendorMeeting.getCode(),
            intentMeeting.getCode(),
            intentDelivery.getCode());

        seedBulkData(
            now,
            kiosk,
            securityDesk,
            security,
            List.of(intentMaintenance, intentServerRoom, intentVendorMeeting, intentMeeting, intentDelivery),
            List.of(financeHost, itHost)
        );
    }

    private void seedBulkData(
        Instant now,
        Device kiosk,
        Device securityDesk,
        User securityUser,
        List<VisitIntent> intents,
        List<HostContact> hosts
    ) {
        int visitorCount = Math.max(properties.getBulkVisitors(), 0);
        int openAlertTarget = Math.max(properties.getBulkOpenAlerts(), 0);
        int reportCount = Math.max(properties.getBulkReports(), 0);

        if (visitorCount == 0 && openAlertTarget == 0 && reportCount == 0) {
            return;
        }

        int generatedOpenAlerts = 0;
        List<DigitalPass> passPool = new ArrayList<>();
        for (int i = 1; i <= visitorCount; i++) {
            String padded = String.format("%04d", i);
            String nik = String.format("39%014d", i);
            Visitor visitor = saveVisitor(
                "Seed Visitor " + padded,
                nik,
                "seed.visitor." + i + "@vigi-gate.local",
                i % 5 == 0 ? "REVIEW_REQUIRED" : "COMPLIANT",
                now
            );

            Instant checkinAt = now.minusSeconds((5L + (i % 720L)) * 60L);
            BigDecimal quality = BigDecimal.valueOf(0.35 + ((i % 60) / 100.0));
            PortraitMedia photo = savePortrait(
                kiosk,
                "https://cdn.vigi-gate.com/portraits/seed-" + i + ".jpg",
                String.format("%064x", i),
                quality,
                checkinAt.minusSeconds(120)
            );

            VisitIntent intent = intents.get(i % intents.size());
            HostContact host = hosts.get(i % hosts.size());

            CheckIn checkIn = saveCheckIn(visitor, intent, photo, kiosk, checkinAt);
            boolean active = i % 4 != 0;
            Instant checkoutAt = active ? null : checkinAt.plusSeconds((25L + (i % 140L)) * 60L);
            String sessionStatus = active ? "ACTIVE" : "CHECKED_OUT";
            VisitSession session = saveSession(checkIn, visitor, host, sessionStatus, checkinAt, checkoutAt, now);

            DigitalPass pass = saveDigitalPass(
                session,
                "VG-SEED-PASS-" + String.format("%05d", i),
                active ? "ISSUED" : "REVOKED",
                checkinAt.plusSeconds(60),
                active ? null : checkoutAt
            );
            passPool.add(pass);

            saveAccessKey(
                session,
                "TEMP_BADGE",
                active ? "ACTIVE" : "REVOKED",
                checkinAt.plusSeconds(120),
                active ? null : checkoutAt
            );

            int riskScore = Math.min(
                99,
                (visitor.getKycStatus().equals("REVIEW_REQUIRED") ? 30 : 5)
                    + (int) ((1.0 - quality.doubleValue()) * 55)
                    + (active ? 10 : 0)
                    + (i % 20)
            );
            String riskLevel = toRiskLevel(riskScore);
            RiskAnalysis analysis = saveRiskAnalysis(
                session,
                riskLevel,
                riskScore,
                "Seeded risk profile for load/testing scenarios.",
                checkinAt.plusSeconds(30)
            );

            saveRiskSignals(analysis, checkinAt.plusSeconds(30), List.of(
                signal(
                    visitor.getKycStatus().equals("REVIEW_REQUIRED") ? "KYC_REVIEW_REQUIRED" : "KYC_COMPLIANT",
                    visitor.getKycStatus().equals("REVIEW_REQUIRED") ? 25 : 0,
                    "kyc_status=" + visitor.getKycStatus()
                ),
                signal("PHOTO_QUALITY", Math.max(0, (int) ((0.75 - quality.doubleValue()) * 100)), "quality_score=" + quality),
                signal("VISIT_PURPOSE", 10 + (i % 15), "purpose=" + intent.getCode())
            ));

            if (generatedOpenAlerts < openAlertTarget && ("HIGH".equals(riskLevel) || i % 7 == 0)) {
                riskAlertRepository.save(
                    RiskAlert.builder()
                        .visitor(visitor)
                        .session(session)
                        .severity("HIGH".equals(riskLevel) ? "HIGH" : "MEDIUM")
                        .title("Seeded risk event #" + i)
                        .description(active ? "Live monitoring queue" : "Historical review queue")
                        .status("OPEN")
                        .createdAt(checkinAt.plusSeconds(180))
                        .closedAt(null)
                        .build()
                );
                generatedOpenAlerts++;
            }

            if (!active && i % 3 == 0) {
                checkOutRepository.save(
                    CheckOut.builder()
                        .session(session)
                        .performedByUser(securityUser)
                        .performedByDevice(securityDesk)
                        .reasonCode("VISIT_COMPLETED")
                        .reasonNote("Seeded historical checkout")
                        .isForced(false)
                        .status("CHECKED_OUT")
                        .checkoutAt(checkoutAt)
                        .createdAt(checkoutAt)
                        .build()
                );
            }
        }

        for (int i = 0; i < Math.min(passPool.size(), 40); i++) {
            DigitalPass pass = passPool.get(i);
            printJobRepository.save(
                PrintJob.builder()
                    .session(pass.getSession())
                    .pass(pass)
                    .requestedByUser(securityUser)
                    .printerId("SEED-PRINTER-" + String.format("%02d", (i % 8) + 1))
                    .copies(1)
                    .status(i % 5 == 0 ? "COMPLETED" : "QUEUED")
                    .queuedAt(now.minusSeconds((long) (i + 1) * 45))
                    .completedAt(i % 5 == 0 ? now.minusSeconds((long) (i + 1) * 30) : null)
                    .build()
            );
        }

        LocalDate baseDate = LocalDate.now(ZoneId.of("Asia/Jakarta")).minusDays(1);
        for (int d = 0; d < reportCount; d++) {
            LocalDate reportDate = baseDate.minusDays(d);
            DailyReport report = dailyReportRepository.save(
                DailyReport.builder()
                    .reportDate(reportDate)
                    .timezone("Asia/Jakarta")
                    .title("Daily Security Intelligence")
                    .subtitle("Comprehensive insights for " + reportDate)
                    .author("Sentinal Core AI v4.2")
                    .confidenceScore(BigDecimal.valueOf(95 + (d % 5)))
                    .lastSyncAt(now.minusSeconds((long) (d + 5) * 60))
                    .generatedAt(now.minusSeconds((long) (d + 5) * 60))
                    .status("READY")
                    .build()
            );

            dailyReportSummaryRepository.save(
                DailyReportSummary.builder()
                    .dailyReport(report)
                    .totalVisitors(120 + (d * 3))
                    .totalVisitorsChangePct(BigDecimal.valueOf((d % 7) + 4))
                    .peakTrafficWindow(d % 2 == 0 ? "09 AM - 11 AM" : "10 AM - 12 PM")
                    .peakWindowSharePct(BigDecimal.valueOf(35 + (d % 10)))
                    .alertsTriggered(2 + (d % 6))
                    .alertsResolutionStatus("All Resolved")
                    .dailyPatterns("Seeded historical traffic profile for analytics baseline and trend testing.")
                    .build()
            );

            dailyReportAnomalyRepository.save(
                DailyReportAnomaly.builder()
                    .dailyReport(report)
                    .severity(d % 3 == 0 ? "MEDIUM" : "LOW")
                    .description("Seeded anomaly sample for report date " + reportDate + ".")
                    .sortOrder(1)
                    .build()
            );
        }

        log.info(
            "DevDataSeeder bulk dataset added. visitors={}, openAlerts={}, reports={}",
            visitorCount,
            generatedOpenAlerts,
            reportCount
        );
    }

    private String toRiskLevel(int score) {
        if (score <= 34) {
            return "LOW";
        }
        if (score <= 69) {
            return "MEDIUM";
        }
        return "HIGH";
    }

    private Role ensureRole(String code, String name) {
        return roleRepository.findByCode(code)
            .orElseGet(() -> roleRepository.save(
                Role.builder()
                    .code(code)
                    .name(name)
                    .description(name)
                    .build()
            ));
    }

    private void ensurePermissionsAttached(Role role) {
        List<String> existingCodes = rolePermissionRepository.findByRoleRoleId(role.getRoleId()).stream()
            .map(entry -> entry.getPermission().getCode())
            .toList();

        Instant now = Instant.now();
        for (String code : AuthPermissions.ALL) {
            if (existingCodes.contains(code)) {
                continue;
            }

            Permission permission = permissionRepository.findByCode(code)
                .orElseGet(() -> permissionRepository.save(
                    Permission.builder()
                        .code(code)
                        .description(code)
                        .build()
                ));

            rolePermissionRepository.save(
                RolePermission.builder()
                    .role(role)
                    .permission(permission)
                    .assignedAt(now)
                    .build()
            );
        }
    }

    private User ensureUser(String fullName, String email, String password, Instant now) {
        User user = userRepository.findByEmailIgnoreCase(email).orElseGet(User::new);
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setAccountStatus("ACTIVE");
        user.setUpdatedAt(now);
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(now);
        }
        return userRepository.save(user);
    }

    private void ensureUserRole(User user, Role role, Instant now) {
        boolean alreadyAssigned = userRoleRepository.findByUserUserId(user.getUserId()).stream()
            .anyMatch(userRole -> userRole.getRole().getRoleId().equals(role.getRoleId()));

        if (!alreadyAssigned) {
            userRoleRepository.save(
                UserRole.builder()
                    .user(user)
                    .role(role)
                    .assignedAt(now)
                    .build()
            );
        }
    }

    private Device saveDevice(String deviceId, String deviceType, String locationLabel, Instant now) {
        Device device = deviceRepository.findById(deviceId).orElseGet(Device::new);
        device.setDeviceId(deviceId);
        device.setDeviceType(deviceType);
        device.setLocationLabel(locationLabel);
        device.setStatus("ACTIVE");
        device.setLastSeenAt(now);
        return deviceRepository.save(device);
    }

    private VisitIntent saveVisitIntent(String code, String label, int sortOrder) {
        VisitIntent intent = visitIntentRepository.findByCode(code).orElseGet(VisitIntent::new);
        intent.setCode(code);
        intent.setLabel(label);
        intent.setSortOrder(sortOrder);
        intent.setIsActive(true);
        return visitIntentRepository.save(intent);
    }

    private HostContact saveHostContact(String fullName, String department, String floor, String phone, String email) {
        HostContact host = hostContactRepository.findByEmailIgnoreCase(email).orElseGet(HostContact::new);
        host.setFullName(fullName);
        host.setDepartment(department);
        host.setFloorLabel(floor);
        host.setPhone(phone);
        host.setEmail(email);
        host.setIsActive(true);
        return hostContactRepository.save(host);
    }

    private Visitor saveVisitor(String fullName, String nik, String email, String kycStatus, Instant now) {
        Visitor visitor = visitorRepository.findByNik(nik).orElseGet(Visitor::new);
        visitor.setFullName(fullName);
        visitor.setNik(nik);
        visitor.setEmail(email);
        visitor.setKycStatus(kycStatus);
        visitor.setUpdatedAt(now);
        if (visitor.getCreatedAt() == null) {
            visitor.setCreatedAt(now);
        }
        return visitorRepository.save(visitor);
    }

    private PortraitMedia savePortrait(
        Device capturedBy,
        String storageUrl,
        String checksum,
        BigDecimal qualityScore,
        Instant createdAt
    ) {
        return portraitMediaRepository.save(
            PortraitMedia.builder()
                .storageUrl(storageUrl)
                .mimeType("image/jpeg")
                .checksumSha256(checksum)
                .qualityScore(qualityScore)
                .processingStatus("READY")
                .expiresAt(createdAt.plusSeconds(60 * 60 * 6))
                .capturedDevice(capturedBy)
                .createdAt(createdAt)
                .build()
        );
    }

    private CheckIn saveCheckIn(
        Visitor visitor,
        VisitIntent purpose,
        PortraitMedia portrait,
        Device device,
        Instant checkinAt
    ) {
        return checkInRepository.save(
            CheckIn.builder()
                .visitor(visitor)
                .purpose(purpose)
                .photo(portrait)
                .device(device)
                .status("CHECKED_IN")
                .checkinAt(checkinAt)
                .createdAt(checkinAt)
                .build()
        );
    }

    private VisitSession saveSession(
        CheckIn checkIn,
        Visitor visitor,
        HostContact hostContact,
        String status,
        Instant checkinAt,
        Instant checkoutAt,
        Instant now
    ) {
        return visitSessionRepository.save(
            VisitSession.builder()
                .checkIn(checkIn)
                .visitor(visitor)
                .hostContact(hostContact)
                .status(status)
                .checkinAt(checkinAt)
                .checkoutAt(checkoutAt)
                .createdAt(checkinAt)
                .updatedAt(now)
                .build()
        );
    }

    private DigitalPass saveDigitalPass(
        VisitSession session,
        String passNumber,
        String status,
        Instant issuedAt,
        Instant revokedAt
    ) {
        return digitalPassRepository.save(
            DigitalPass.builder()
                .session(session)
                .passNumber(passNumber)
                .status(status)
                .issuedAt(issuedAt)
                .revokedAt(revokedAt)
                .build()
        );
    }

    private AccessKey saveAccessKey(VisitSession session, String keyType, String status, Instant issuedAt, Instant revokedAt) {
        return accessKeyRepository.save(
            AccessKey.builder()
                .session(session)
                .keyType(keyType)
                .status(status)
                .issuedAt(issuedAt)
                .revokedAt(revokedAt)
                .build()
        );
    }

    private RiskAnalysis saveRiskAnalysis(VisitSession session, String level, int score, String summary, Instant computedAt) {
        return riskAnalysisRepository.save(
            RiskAnalysis.builder()
                .session(session)
                .riskLevel(level)
                .riskScore(score)
                .summary(summary)
                .modelVersion("rules-v1")
                .computedAt(computedAt)
                .build()
        );
    }

    private void saveRiskSignals(RiskAnalysis analysis, Instant observedAt, List<SignalSeed> seeds) {
        for (SignalSeed seed : seeds) {
            riskSignalRepository.save(
                RiskSignal.builder()
                    .riskAnalysis(analysis)
                    .signalType(seed.signalType())
                    .weight(BigDecimal.valueOf(seed.weight()))
                    .valueText(seed.valueText())
                    .observedAt(observedAt)
                    .build()
            );
        }
    }

    private SignalSeed signal(String signalType, int weight, String valueText) {
        return new SignalSeed(signalType, weight, valueText);
    }

    private record SignalSeed(String signalType, int weight, String valueText) {
    }
}
