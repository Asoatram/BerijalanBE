package com.example.berijalanassesment.service;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "risk.engine")
@Getter
@Setter
public class RiskEngineProperties {

    private String timezone = "Asia/Jakarta";
    private String modelVersion = "rules-v1";

    private int lowMax = 34;
    private int mediumMax = 69;

    private int businessHourStart = 7;
    private int businessHourEnd = 20;

    private int weightKycReviewRequired = 25;
    private int weightKycBlocked = 40;

    private BigDecimal photoHighQualityMin = BigDecimal.valueOf(0.85);
    private BigDecimal photoMediumQualityMin = BigDecimal.valueOf(0.75);
    private BigDecimal photoLowQualityMin = BigDecimal.valueOf(0.65);
    private int weightPhotoMedium = 10;
    private int weightPhotoLow = 20;
    private int weightPhotoVeryLow = 35;

    private int weightPurposeMeeting = 5;
    private int weightPurposeDelivery = 15;
    private int weightPurposeMaintenance = 25;
    private int weightPurposeOther = 10;

    private int weightOffHours = 15;
    private int weightRepeatDaily = 20;
    private int weightRepeatWeekly = 15;
    private int weightOpenHighAlert = 30;
    private int weightOpenMediumAlert = 15;
}

