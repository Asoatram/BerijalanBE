package com.example.berijalanassesment.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "openai")
@Getter
@Setter
public class OpenAiProperties {

    private String apiKey;
    private String baseUrl = "https://api.openai.com/v1";
    private String model = "gpt-4.1-mini";
    private int timeoutMs = 20000;
    private String imageDetail = "low";
    private String summaryModel;
    private Integer summaryTimeoutMs;
}
