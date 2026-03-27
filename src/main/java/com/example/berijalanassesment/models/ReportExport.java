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
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "report_exports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportExport {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "export_id", nullable = false, updatable = false)
    private UUID exportId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "daily_report_id", nullable = false)
    private DailyReport dailyReport;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exported_by_user_id", nullable = false)
    private User exportedByUser;

    @Column(name = "format", nullable = false)
    private String format;

    @Column(name = "include_sections_json")
    private String includeSectionsJson;

    @Column(name = "download_url", nullable = false)
    private String downloadUrl;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
