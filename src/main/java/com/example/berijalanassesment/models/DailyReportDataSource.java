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
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "daily_report_data_sources")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyReportDataSource {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "data_source_id", nullable = false, updatable = false)
    private UUID dataSourceId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "daily_report_id", nullable = false)
    private DailyReport dailyReport;

    @Column(name = "source_name", nullable = false)
    private String sourceName;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;
}
