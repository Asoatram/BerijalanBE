package com.example.berijalanassesment.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "daily_report_summaries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyReportSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "daily_report_summary_id", nullable = false, updatable = false)
    private UUID dailyReportSummaryId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "daily_report_id", nullable = false, unique = true)
    private DailyReport dailyReport;

    @Column(name = "total_visitors", nullable = false)
    private Integer totalVisitors;

    @Column(name = "total_visitors_change_pct")
    private BigDecimal totalVisitorsChangePct;

    @Column(name = "peak_traffic_window")
    private String peakTrafficWindow;

    @Column(name = "peak_window_share_pct")
    private BigDecimal peakWindowSharePct;

    @Column(name = "alerts_triggered", nullable = false)
    private Integer alertsTriggered;

    @Column(name = "alerts_resolution_status")
    private String alertsResolutionStatus;

    @Column(name = "daily_patterns", columnDefinition = "TEXT")
    private String dailyPatterns;
}
