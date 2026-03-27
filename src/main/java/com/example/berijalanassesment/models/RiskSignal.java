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
@Table(name = "risk_signals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskSignal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "signal_id", nullable = false, updatable = false)
    private UUID signalId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "risk_analysis_id", nullable = false)
    private RiskAnalysis riskAnalysis;

    @Column(name = "signal_type", nullable = false)
    private String signalType;

    @Column(name = "weight", nullable = false)
    private BigDecimal weight;

    @Column(name = "value_text")
    private String valueText;

    @Column(name = "observed_at")
    private Instant observedAt;
}
