package com.example.berijalanassesment.service;

import com.example.berijalanassesment.controller.support.ApiException;
import com.example.berijalanassesment.dto.screen.VisitorRegistrationDtos;
import com.example.berijalanassesment.models.CheckIn;
import com.example.berijalanassesment.models.Device;
import com.example.berijalanassesment.models.PortraitMedia;
import com.example.berijalanassesment.models.VisitIntent;
import com.example.berijalanassesment.models.VisitSession;
import com.example.berijalanassesment.models.Visitor;
import com.example.berijalanassesment.repository.CheckInRepository;
import com.example.berijalanassesment.repository.DeviceRepository;
import com.example.berijalanassesment.repository.PortraitMediaRepository;
import com.example.berijalanassesment.repository.VisitIntentRepository;
import com.example.berijalanassesment.repository.VisitSessionRepository;
import com.example.berijalanassesment.repository.VisitorRepository;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class VisitorRegistrationService {

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of("image/jpeg", "image/png");
    private static final double MIN_QUALITY_SCORE = 0.65;
    private static final double NO_FACE_QUALITY_SCORE = 0.0;

    private final VisitIntentRepository visitIntentRepository;
    private final PortraitMediaRepository portraitMediaRepository;
    private final VisitorRepository visitorRepository;
    private final DeviceRepository deviceRepository;
    private final CheckInRepository checkInRepository;
    private final VisitSessionRepository visitSessionRepository;
    private final RiskEngineService riskEngineService;
    private final PortraitQualityEvaluator portraitQualityEvaluator;

    public VisitorRegistrationService(
        VisitIntentRepository visitIntentRepository,
        PortraitMediaRepository portraitMediaRepository,
        VisitorRepository visitorRepository,
        DeviceRepository deviceRepository,
        CheckInRepository checkInRepository,
        VisitSessionRepository visitSessionRepository,
        RiskEngineService riskEngineService,
        PortraitQualityEvaluator portraitQualityEvaluator
    ) {
        this.visitIntentRepository = visitIntentRepository;
        this.portraitMediaRepository = portraitMediaRepository;
        this.visitorRepository = visitorRepository;
        this.deviceRepository = deviceRepository;
        this.checkInRepository = checkInRepository;
        this.visitSessionRepository = visitSessionRepository;
        this.riskEngineService = riskEngineService;
        this.portraitQualityEvaluator = portraitQualityEvaluator;
    }

    public VisitorRegistrationDtos.VisitIntentsResponse getVisitIntents(boolean active) {
        List<VisitIntent> intents = active
            ? visitIntentRepository.findByIsActiveTrueOrderBySortOrderAsc()
            : visitIntentRepository.findAll();

        return VisitorRegistrationDtos.VisitIntentsResponse.builder()
            .data(
                intents.stream()
                    .map(intent -> VisitorRegistrationDtos.VisitIntentItem.builder()
                        .id(intent.getPurposeId())
                        .code(intent.getCode())
                        .label(intent.getLabel())
                        .sortOrder(intent.getSortOrder())
                        .build())
                    .toList()
            )
            .build();
    }

    public VisitorRegistrationDtos.CheckinPolicyResponse getCheckinPolicy() {
        return VisitorRegistrationDtos.CheckinPolicyResponse.builder()
            .data(
                VisitorRegistrationDtos.CheckinPolicy.builder()
                    .nik(VisitorRegistrationDtos.PolicyRuleNik.builder().required(true).digits(16).build())
                    .fullName(
                        VisitorRegistrationDtos.PolicyRuleFullName.builder()
                            .required(true)
                            .minLength(3)
                            .maxLength(150)
                            .build()
                    )
                    .photo(
                        VisitorRegistrationDtos.PolicyRulePhoto.builder()
                            .required(true)
                            .allowedMimeTypes(List.copyOf(ALLOWED_MIME_TYPES))
                            .maxSizeBytes(5_242_880L)
                            .minQualityScore(MIN_QUALITY_SCORE)
                            .build()
                    )
                    .build()
            )
            .build();
    }

    public ResponseEntity<VisitorRegistrationDtos.UploadPortraitResponse> uploadPortrait(
        VisitorRegistrationDtos.UploadPortraitRequest request
    ) {
        if (!ALLOWED_MIME_TYPES.contains(request.getMimeType())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_IMAGE_FORMAT", "Unsupported image format");
        }

        int estimatedBytes = (request.getContentBase64().length() * 3) / 4;
        if (estimatedBytes > 5_242_880) {
            throw new ApiException(HttpStatus.PAYLOAD_TOO_LARGE, "IMAGE_TOO_LARGE", "Image exceeds max size");
        }

        PortraitQualityResult quality = portraitQualityEvaluator.evaluate(request.getMimeType(), request.getContentBase64());
        double normalizedQualityScore = quality.faceDetected() ? quality.qualityScore() : NO_FACE_QUALITY_SCORE;

        PortraitMedia media = PortraitMedia.builder()
            .mimeType(request.getMimeType())
            .checksumSha256(request.getChecksumSha256())
            .processingStatus("READY")
            .qualityScore(BigDecimal.valueOf(normalizedQualityScore))
            .expiresAt(Instant.now().plusSeconds(3600))
            .createdAt(Instant.now())
            .storageUrl("memory://portraits/" + UUID.randomUUID())
            .build();

        PortraitMedia saved = portraitMediaRepository.save(media);

        return ResponseEntity.status(HttpStatus.CREATED).body(
            VisitorRegistrationDtos.UploadPortraitResponse.builder()
                .data(
                    VisitorRegistrationDtos.UploadPortraitData.builder()
                        .photoId(saved.getPhotoId())
                        .status(saved.getProcessingStatus())
                        .qualityScore(saved.getQualityScore() == null ? null : saved.getQualityScore().doubleValue())
                        .expiresAt(saved.getExpiresAt())
                        .build()
                )
                .build()
        );
    }

    public ResponseEntity<VisitorRegistrationDtos.CreateCheckInResponse> createCheckIn(
        VisitorRegistrationDtos.CreateCheckInRequest request,
        String idempotencyKey
    ) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Idempotency-Key header is required");
        }

        VisitIntent intent = visitIntentRepository.findById(request.getPurposeId())
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "PURPOSE_NOT_FOUND", "Visit purpose not found"));

        PortraitMedia photo = portraitMediaRepository.findByPhotoIdAndProcessingStatus(request.getPhotoId(), "READY")
            .orElseThrow(() -> new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "PHOTO_NOT_READY", "Photo is not ready"));

        Visitor visitor = visitorRepository.findByNik(request.getNik()).orElseGet(() -> visitorRepository.save(
            Visitor.builder()
                .fullName(request.getFullName())
                .nik(request.getNik())
                .email(null)
                .kycStatus("COMPLIANT")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build()
        ));

        if (visitSessionRepository.existsByVisitorVisitorIdAndStatus(visitor.getVisitorId(), "ACTIVE")) {
            throw new ApiException(HttpStatus.CONFLICT, "ACTIVE_VISIT_EXISTS", "Visitor already has an active session");
        }

        Device device = deviceRepository.findById(request.getDeviceId()).orElseGet(() -> deviceRepository.save(
            Device.builder()
                .deviceId(request.getDeviceId())
                .deviceType("KIOSK")
                .locationLabel("Unknown")
                .status("ACTIVE")
                .lastSeenAt(Instant.now())
                .build()
        ));

        CheckIn checkIn = checkInRepository.save(
            CheckIn.builder()
                .visitor(visitor)
                .purpose(intent)
                .photo(photo)
                .device(device)
                .status("CHECKED_IN")
                .checkinAt(Instant.now())
                .createdAt(Instant.now())
                .build()
        );

        VisitSession session = visitSessionRepository.save(
            VisitSession.builder()
                .checkIn(checkIn)
                .visitor(visitor)
                .hostContact(null)
                .status("ACTIVE")
                .checkinAt(checkIn.getCheckinAt())
                .checkoutAt(null)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build()
        );

        riskEngineService.evaluateAndPersist(session, "CHECK_IN", Instant.now());

        VisitorRegistrationDtos.CreateCheckInData data = VisitorRegistrationDtos.CreateCheckInData.builder()
            .checkinId(checkIn.getCheckinId())
            .visitorId(visitor.getVisitorId())
            .sessionId(session.getSessionId())
            .status(checkIn.getStatus())
            .checkinAt(checkIn.getCheckinAt())
            .message(Objects.equals(checkIn.getStatus(), "PENDING_REVIEW")
                ? "Check-in submitted and awaiting security approval"
                : null)
            .build();

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(VisitorRegistrationDtos.CreateCheckInResponse.builder().data(data).build());
    }

    public ResponseEntity<VisitorRegistrationDtos.CreateCheckInResponse> createCheckInMultipart(
        String fullName,
        String nik,
        UUID purposeId,
        String deviceId,
        MultipartFile photo,
        String idempotencyKey
    ) {
        validateMultipartPayload(fullName, nik, purposeId, deviceId, photo);

        String mimeType = photo.getContentType() == null ? "" : photo.getContentType().trim().toLowerCase();
        if (!ALLOWED_MIME_TYPES.contains(mimeType)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_IMAGE_FORMAT", "Unsupported image format");
        }
        if (photo.getSize() > 5_242_880) {
            throw new ApiException(HttpStatus.PAYLOAD_TOO_LARGE, "IMAGE_TOO_LARGE", "Image exceeds max size");
        }

        byte[] bytes;
        try {
            bytes = photo.getBytes();
        } catch (IOException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_IMAGE", "Failed to read uploaded image");
        }
        String contentBase64 = Base64.getEncoder().encodeToString(bytes);
        String checksum = sha256Hex(bytes);

        PortraitQualityResult quality = portraitQualityEvaluator.evaluate(mimeType, contentBase64);
        double normalizedQualityScore = quality.faceDetected() ? quality.qualityScore() : NO_FACE_QUALITY_SCORE;

        PortraitMedia media = portraitMediaRepository.save(
            PortraitMedia.builder()
                .mimeType(mimeType)
                .checksumSha256(checksum)
                .processingStatus("READY")
                .qualityScore(BigDecimal.valueOf(normalizedQualityScore))
                .expiresAt(Instant.now().plusSeconds(3600))
                .createdAt(Instant.now())
                .storageUrl("memory://portraits/" + UUID.randomUUID())
                .build()
        );

        VisitorRegistrationDtos.CreateCheckInRequest request = VisitorRegistrationDtos.CreateCheckInRequest.builder()
            .fullName(fullName.trim())
            .nik(nik.trim())
            .purposeId(purposeId)
            .photoId(media.getPhotoId())
            .deviceId(deviceId.trim())
            .build();

        return createCheckIn(request, idempotencyKey);
    }

    private void validateMultipartPayload(
        String fullName,
        String nik,
        UUID purposeId,
        String deviceId,
        MultipartFile photo
    ) {
        if (fullName == null || fullName.isBlank() || fullName.trim().length() < 3 || fullName.trim().length() > 150) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "fullName must be between 3 and 150 characters");
        }
        if (nik == null || !nik.matches("^\\d{16}$")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "nik must be exactly 16 digits");
        }
        if (purposeId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "purposeId is required");
        }
        if (deviceId == null || deviceId.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "deviceId is required");
        }
        if (photo == null || photo.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "photo is required");
        }
    }

    private String sha256Hex(byte[] payload) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(payload);
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }
}
