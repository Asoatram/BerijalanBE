package com.example.berijalanassesment.service;

import java.util.List;

public record RiskSummaryInput(
    int riskScore,
    String riskLevel,
    List<RiskSummarySignal> signals
) {
}
