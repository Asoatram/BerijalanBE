package com.example.berijalanassesment.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "portrait_media")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortraitMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "photo_id", nullable = false, updatable = false)
    private UUID photoId;

    @Column(name = "storage_url")
    private String storageUrl;

    @Column(name = "mime_type", nullable = false)
    private String mimeType;

    @Column(name = "checksum_sha256", nullable = false)
    private String checksumSha256;

    @Column(name = "quality_score")
    private BigDecimal qualityScore;

    @Column(name = "processing_status", nullable = false)
    private String processingStatus;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "captured_device_id")
    private Device capturedDevice;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
