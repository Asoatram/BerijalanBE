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
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "risk_analyses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "risk_analysis_id", nullable = false, updatable = false)
    private UUID riskAnalysisId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false, unique = true)
    private VisitSession session;

    @Column(name = "risk_level", nullable = false)
    private String riskLevel;

    @Column(name = "risk_score", nullable = false)
    private Integer riskScore;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "model_version")
    private String modelVersion;

    @Column(name = "computed_at", nullable = false)
    private Instant computedAt;
}
