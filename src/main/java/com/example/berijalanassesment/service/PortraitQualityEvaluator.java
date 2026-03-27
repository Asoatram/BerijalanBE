package com.example.berijalanassesment.service;

public interface PortraitQualityEvaluator {

    PortraitQualityResult evaluate(String mimeType, String contentBase64);
}
