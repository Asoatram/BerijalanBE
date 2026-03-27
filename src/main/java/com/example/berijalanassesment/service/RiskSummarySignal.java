package com.example.berijalanassesment.service;

public record RiskSummarySignal(
    String type,
    int weight,
    String value
) {
}
