package com.example.berijalanassesment.service;

public interface SummaryGenerator {

    String generateRiskSummary(RiskSummaryInput input);

    String generateDailyPatterns(DailyPatternsInput input);
}
