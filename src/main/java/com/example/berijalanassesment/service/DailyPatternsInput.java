package com.example.berijalanassesment.service;

import java.time.LocalDate;
import java.util.List;

public record DailyPatternsInput(
    LocalDate reportDate,
    String timezone,
    Integer totalVisitors,
    Integer alertsTriggered,
    String peakTrafficWindow,
    Integer peakWindowSharePct,
    String alertsResolutionStatus,
    List<String> anomalies,
    List<String> recommendations,
    List<String> operationalFacts
) {
}
