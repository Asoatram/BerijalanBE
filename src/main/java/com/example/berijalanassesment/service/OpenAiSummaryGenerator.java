package com.example.berijalanassesment.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.stereotype.Service;

@Service
public class OpenAiSummaryGenerator implements SummaryGenerator {

    private final OpenAiProperties properties;
    private final HttpClient httpClient;
    private final JsonParser jsonParser = JsonParserFactory.getJsonParser();

    public OpenAiSummaryGenerator(OpenAiProperties properties) {
        this.properties = properties;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(resolveTimeoutMs()))
            .build();
    }

    @Override
    public String generateRiskSummary(RiskSummaryInput input) {
        String prompt = buildRiskPrompt(input);
        return requestSummaryText(prompt);
    }

    @Override
    public String generateDailyPatterns(DailyPatternsInput input) {
        String prompt = buildDailyPatternsPrompt(input);
        return requestSummaryText(prompt);
    }

    private String requestSummaryText(String prompt) {
        String apiKey = properties.getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("OpenAI API key is not configured");
        }

        HttpRequest request = HttpRequest.newBuilder()
            .uri(buildResponsesUri())
            .timeout(Duration.ofMillis(resolveTimeoutMs()))
            .header("Authorization", "Bearer " + apiKey.trim())
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(buildRequestBody(prompt)))
            .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("OpenAI summary request was interrupted", ex);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to call OpenAI summary API", ex);
        }

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("OpenAI summary API returned non-success status: " + response.statusCode());
        }

        return parseSummary(response.body());
    }

    private URI buildResponsesUri() {
        String baseUrl = properties.getBaseUrl() == null ? "" : properties.getBaseUrl().trim();
        if (baseUrl.endsWith("/")) {
            return URI.create(baseUrl + "responses");
        }
        return URI.create(baseUrl + "/responses");
    }

    private String buildRequestBody(String prompt) {
        return """
            {
              "model": "%s",
              "input": [
                {
                  "role": "user",
                  "content": [
                    {
                      "type": "input_text",
                      "text": "%s"
                    }
                  ]
                }
              ],
              "text": {
                "format": {
                  "type": "json_object"
                }
              }
            }
            """.formatted(escapeJson(resolveSummaryModel()), escapeJson(prompt));
    }

    private String parseSummary(String responseBody) {
        Map<String, Object> root;
        try {
            root = jsonParser.parseMap(responseBody);
        } catch (RuntimeException ex) {
            throw new IllegalStateException("Failed to parse OpenAI summary response", ex);
        }

        String outputText = extractOutputText(root);
        if (outputText == null || outputText.isBlank()) {
            throw new IllegalStateException("OpenAI summary response is missing output_text");
        }

        Map<String, Object> result;
        try {
            result = jsonParser.parseMap(outputText);
        } catch (RuntimeException ex) {
            throw new IllegalStateException("OpenAI summary output is not valid JSON", ex);
        }

        Object summaryNode = result.get("summary_text");
        if (!(summaryNode instanceof String summary) || summary.isBlank()) {
            throw new IllegalStateException("OpenAI summary output missing summary_text");
        }

        return summary.trim();
    }

    private String buildRiskPrompt(RiskSummaryInput input) {
        String signals = input.signals().stream()
            .sorted((left, right) -> Integer.compare(right.weight(), left.weight()))
            .limit(6)
            .map(signal -> signal.type() + " (weight=" + signal.weight() + ", value=" + signal.value() + ")")
            .collect(Collectors.joining("; "));

        return """
            You are a security risk summarizer for visitor check-in.
            Generate exactly one concise sentence in plain English.
            Return only valid JSON with this exact schema:
            {
              "summary_text": "string"
            }

            Rules:
            - Maximum 24 words.
            - Mention the strongest 1-2 risk drivers.
            - Do not mention internal system details or model names.
            - Professional tone for security operators.

            Input:
            - risk_score: %d
            - risk_level: %s
            - signals: %s
            """.formatted(input.riskScore(), input.riskLevel(), signals);
    }

    private String buildDailyPatternsPrompt(DailyPatternsInput input) {
        String anomalyText = input.anomalies() == null || input.anomalies().isEmpty()
            ? "none"
            : String.join(" | ", input.anomalies().stream().limit(3).toList());
        String recommendationText = input.recommendations() == null || input.recommendations().isEmpty()
            ? "none"
            : String.join(" | ", input.recommendations().stream().limit(3).toList());
        String operationalFacts = input.operationalFacts() == null || input.operationalFacts().isEmpty()
            ? "none"
            : String.join(" | ", input.operationalFacts().stream().limit(12).toList());

        return """
            You are a security operations analyst writing a daily intelligence narrative.
            Generate 2-3 sentences in plain English for a dashboard card.
            Return only valid JSON with this exact schema:
            {
              "summary_text": "string"
            }

            Rules:
            - Maximum 90 words.
            - Mention traffic pattern and alert context.
            - Include at least 3 concrete numeric facts from the provided input.
            - If anomalies exist, mention the most severe one.
            - If no anomalies exist, explicitly state no anomalies were detected.
            - Keep language operational and factual.
            - Do not add data not present in inputs.

            Input:
            - report_date: %s
            - timezone: %s
            - total_visitors: %s
            - alerts_triggered: %s
            - peak_traffic_window: %s
            - peak_window_share_pct: %s
            - alerts_resolution_status: %s
            - operational_facts: %s
            - anomalies: %s
            - recommendations: %s
            """.formatted(
            input.reportDate(),
            input.timezone(),
            safeValue(input.totalVisitors()),
            safeValue(input.alertsTriggered()),
            safeValue(input.peakTrafficWindow()),
            safeValue(input.peakWindowSharePct()),
            safeValue(input.alertsResolutionStatus()),
            operationalFacts,
            anomalyText,
            recommendationText
        );
    }

    private String extractOutputText(Map<String, Object> root) {
        Object outputText = root.get("output_text");
        if (outputText instanceof String text && !text.isBlank()) {
            return text;
        }

        Object output = root.get("output");
        if (!(output instanceof List<?> rawOutput)) {
            return null;
        }

        for (Object item : rawOutput) {
            if (!(item instanceof Map<?, ?> itemMap)) {
                continue;
            }
            Object content = itemMap.get("content");
            if (!(content instanceof List<?> rawContent)) {
                continue;
            }
            for (Object chunk : rawContent) {
                if (!(chunk instanceof Map<?, ?> chunkMap)) {
                    continue;
                }
                Object type = chunkMap.get("type");
                Object text = chunkMap.get("text");
                if (type instanceof String chunkType && text instanceof String chunkText && !chunkText.isBlank()) {
                    if ("output_text".equals(chunkType) || "text".equals(chunkType)) {
                        return chunkText;
                    }
                }
            }
        }

        return null;
    }

    private String resolveSummaryModel() {
        String summaryModel = properties.getSummaryModel();
        if (summaryModel != null && !summaryModel.isBlank()) {
            return summaryModel.trim();
        }
        return properties.getModel();
    }

    private int resolveTimeoutMs() {
        Integer summaryTimeoutMs = properties.getSummaryTimeoutMs();
        if (summaryTimeoutMs != null && summaryTimeoutMs > 0) {
            return summaryTimeoutMs;
        }
        return properties.getTimeoutMs();
    }

    private String safeValue(Object value) {
        if (value == null) {
            return "n/a";
        }
        return value.toString();
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\r", "\\r")
            .replace("\n", "\\n");
    }
}
