package com.campus.campus_system.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class HttpAiSemanticRecommendationClient implements AiSemanticRecommendationClient {
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String baseUrl;
    private final long readTimeoutMs;

    public HttpAiSemanticRecommendationClient(ObjectMapper objectMapper,
                                              @Value("${ai.recommend.base-url:http://localhost:8001}") String baseUrl,
                                              @Value("${ai.recommend.read-timeout-ms:3000}") long readTimeoutMs) {
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl;
        this.readTimeoutMs = readTimeoutMs;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(1000))
                .build();
    }

    @Override
    public List<SemanticScoreResult> scoreItems(SemanticScoreRequest request) {
        try {
            String payload = objectMapper.writeValueAsString(request);
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/score"))
                    .timeout(Duration.ofMillis(readTimeoutMs))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("AI service returned status " + response.statusCode());
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode scoresNode = root.get("scores");
            if (scoresNode == null || !scoresNode.isArray()) {
                throw new IllegalStateException("AI service response missing scores array");
            }

            List<SemanticScoreResult> results = new ArrayList<>();
            for (JsonNode itemNode : scoresNode) {
                String itemId = itemNode.path("itemId").asText();
                double semanticScore = itemNode.path("semanticScore").asDouble(0.0);
                results.add(new SemanticScoreResult(itemId, semanticScore));
            }
            return results;
        } catch (IOException ex) {
            throw new IllegalStateException("AI service unavailable", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("AI service unavailable", ex);
        }
    }
}
