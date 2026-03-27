package com.example.berijalanassesment.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "daily_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyReport {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "daily_report_id", nullable = false, updatable = false)
    private UUID dailyReportId;

    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    @Column(name = "timezone", nullable = false)
    private String timezone;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "subtitle")
    private String subtitle;

    @Column(name = "author")
    private String author;

    @Column(name = "confidence_score")
    private BigDecimal confidenceScore;

    @Column(name = "last_sync_at")
    private Instant lastSyncAt;

    @Column(name = "generated_at", nullable = false)
    private Instant generatedAt;

    @Column(name = "status", nullable = false)
    private String status;
}
