package com.example.berijalanassesment.service;

import com.example.berijalanassesment.controller.support.ApiException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class OpenAiPortraitQualityEvaluator implements PortraitQualityEvaluator {

    private final OpenAiProperties properties;
    private final HttpClient httpClient;
    private final JsonParser jsonParser = JsonParserFactory.getJsonParser();

    public OpenAiPortraitQualityEvaluator(OpenAiProperties properties) {
        this.properties = properties;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(properties.getTimeoutMs()))
            .build();
    }

    @Override
    public PortraitQualityResult evaluate(String mimeType, String contentBase64) {
        String apiKey = properties.getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw mediaProcessingFailed("OpenAI API key is not configured");
        }

        String payload = buildRequestBody(mimeType, contentBase64);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(buildResponsesUri())
            .timeout(Duration.ofMillis(properties.getTimeoutMs()))
            .header("Authorization", "Bearer " + apiKey.trim())
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(payload))
            .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw mediaProcessingFailed("OpenAI request was interrupted");
        } catch (IOException ex) {
            throw mediaProcessingFailed("Failed to call OpenAI API");
        }

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw mediaProcessingFailed("OpenAI API returned non-success status: " + response.statusCode());
        }

        return parseResponse(response.body());
    }

    private URI buildResponsesUri() {
        String baseUrl = properties.getBaseUrl() == null ? "" : properties.getBaseUrl().trim();
        if (baseUrl.endsWith("/")) {
            return URI.create(baseUrl + "responses");
        }
        return URI.create(baseUrl + "/responses");
    }

    private String buildRequestBody(String mimeType, String contentBase64) {
        String prompt = """
            Evaluate portrait image quality for check-in identity verification.
            Return only valid JSON object with this exact schema:
            {
              "face_detected": boolean,
              "quality_score": number,
              "issues": string[]
            }
            Rules:
            - quality_score must be between 0.0 and 1.0
            - set face_detected=false if no clear human face is visible
            - include concise issue codes in issues, such as BLUR, LOW_LIGHT, FACE_NOT_CENTERED, OCCLUDED_FACE, MULTIPLE_FACES, LOW_RESOLUTION
            - if image is good, issues can be empty
            """;

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
                    },
                    {
                      "type": "input_image",
                      "image_url": "%s",
                      "detail": "%s"
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
            """
            .formatted(
                escapeJson(properties.getModel()),
                escapeJson(prompt),
                escapeJson("data:" + mimeType + ";base64," + contentBase64),
                escapeJson(properties.getImageDetail())
            );
    }

    private PortraitQualityResult parseResponse(String responseBody) {
        Map<String, Object> root;
        try {
            root = jsonParser.parseMap(responseBody);
        } catch (RuntimeException ex) {
            throw mediaProcessingFailed("Failed to parse OpenAI response");
        }

        String outputText = extractOutputText(root);
        if (outputText == null || outputText.isBlank()) {
            throw mediaProcessingFailed("OpenAI response is missing output_text");
        }

        Map<String, Object> result;
        try {
            result = jsonParser.parseMap(outputText);
        } catch (RuntimeException ex) {
            throw mediaProcessingFailed("OpenAI output is not valid JSON");
        }

        Object faceDetectedNode = result.get("face_detected");
        Object scoreNode = result.get("quality_score");
        Object issuesNode = result.get("issues");

        if (!(faceDetectedNode instanceof Boolean faceDetected)) {
            throw mediaProcessingFailed("OpenAI output missing face_detected boolean");
        }
        if (!(scoreNode instanceof Number score)) {
            throw mediaProcessingFailed("OpenAI output missing quality_score number");
        }

        double qualityScore = score.doubleValue();
        qualityScore = Math.max(0.0, Math.min(1.0, qualityScore));

        List<String> issues = new ArrayList<>();
        if (issuesNode instanceof List<?> rawIssues) {
            for (Object issue : rawIssues) {
                if (issue instanceof String issueCode && !issueCode.isBlank()) {
                    issues.add(issueCode.trim());
                }
            }
        }

        return new PortraitQualityResult(
            qualityScore,
            faceDetected,
            issues,
            properties.getModel()
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

    private ApiException mediaProcessingFailed(String message) {
        return new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "MEDIA_PROCESSING_FAILED", message);
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
