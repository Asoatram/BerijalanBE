package com.example.berijalanassesment.service;

import java.util.List;

public record PortraitQualityResult(
    double qualityScore,
    boolean faceDetected,
    List<String> issues,
    String model
) {
}
