package com.leftoverloop.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leftoverloop.dto.AiMatchRequest;
import com.leftoverloop.dto.AiMatchResponse;
import com.leftoverloop.dto.FoodDonationDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AiRecommendationService {

    @Value("${ai.api.key:}")
    private String apiKey;

    @Value("${ai.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent}")
    private String apiUrl;

    @Autowired
    private FoodDonationService donationService;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiMatchResponse getRecommendations(AiMatchRequest request) {
        List<FoodDonationDto> activeDonations = donationService.getAllActiveDonations(null, null);

        if (activeDonations.isEmpty()) {
            return new AiMatchResponse(
                    "LeftoverLoop AI Engine",
                    false,
                    "No active food donations currently available for matching.",
                    Collections.emptyList()
            );
        }

        // Try Live Cloud AI API if API key is configured
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            try {
                return getLiveCloudAiRecommendations(request, activeDonations);
            } catch (Exception e) {
                // Log and gracefully fall back to internal scoring algorithm
                System.err.println("Cloud AI Service call failed: " + e.getMessage() + ". Falling back to Smart Match Engine.");
            }
        }

        // Fallback: Smart Heuristic AI Matching Algorithm
        return getHeuristicAiRecommendations(request, activeDonations);
    }

    private AiMatchResponse getLiveCloudAiRecommendations(AiMatchRequest request, List<FoodDonationDto> donations) throws Exception {
        String fullUrl = apiUrl + "?key=" + apiKey;

        StringBuilder prompt = new StringBuilder();
        prompt.append("You are an intelligent food redistribution AI optimizer. ");
        prompt.append("Match the following receiver request with available donations.\n");
        prompt.append("Receiver Request Details:\n");
        prompt.append("- Preferred Category: ").append(request.getPreferredCategory() != null ? request.getPreferredCategory() : "Any").append("\n");
        prompt.append("- Location: ").append(request.getLocation() != null ? request.getLocation() : "Any").append("\n");
        prompt.append("- Requested Quantity: ").append(request.getRequestedQuantity() != null ? request.getRequestedQuantity() : 1).append("\n");
        prompt.append("- Receiver Type: ").append(request.getReceiverType() != null ? request.getReceiverType() : "INDIVIDUAL").append("\n\n");

        prompt.append("Available Food Donations:\n");
        for (FoodDonationDto d : donations) {
            prompt.append(String.format("ID %d: '%s' | Cat: %s | Qty Left: %d %s | Expiry: %s | Location: %s\n",
                    d.getId(), d.getTitle(), d.getCategory(), d.getRemainingQuantity(), d.getUnit(), d.getExpiryDate(), d.getLocation()));
        }

        prompt.append("\nEvaluate and return JSON with structure: {\"summary\": \"...\", \"rankings\": [{\"id\": 1, \"score\": 95, \"reason\": \"...\", \"urgency\": \"HIGH\"}]}");

        Map<String, Object> requestBody = new HashMap<>();
        List<Map<String, Object>> contents = new ArrayList<>();
        Map<String, Object> contentMap = new HashMap<>();
        List<Map<String, Object>> parts = new ArrayList<>();
        Map<String, Object> partMap = new HashMap<>();
        partMap.put("text", prompt.toString());
        parts.add(partMap);
        contentMap.put("parts", parts);
        contents.add(contentMap);
        requestBody.put("contents", contents);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(fullUrl, entity, String.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            JsonNode root = objectMapper.readTree(response.getBody());
            String text = root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();

            String aiSummary = "Live Cloud AI analyzed " + donations.size() + " listings against demand and urgency.";
            Map<Long, Map<String, Object>> aiRankingsMap = new HashMap<>();

            try {
                // Strip markdown backticks if returned (e.g. ```json ... ```)
                String cleanJson = text.replaceAll("(?s)```json\\s*(.*?)\\s*```", "$1").replaceAll("(?s)```\\s*(.*?)\\s*```", "$1").trim();
                JsonNode parsedAi = objectMapper.readTree(cleanJson);
                if (parsedAi.has("summary")) {
                    aiSummary = parsedAi.get("summary").asText();
                }
                if (parsedAi.has("rankings") && parsedAi.get("rankings").isArray()) {
                    for (JsonNode r : parsedAi.get("rankings")) {
                        long id = r.path("id").asLong();
                        Map<String, Object> details = new HashMap<>();
                        details.put("score", r.path("score").asDouble(80.0));
                        details.put("reason", r.path("reason").asText("AI Cloud Model Match"));
                        details.put("urgency", r.path("urgency").asText("NORMAL"));
                        aiRankingsMap.put(id, details);
                    }
                }
            } catch (Exception parseEx) {
                // Fallback to text explanation if JSON parsing failed
                aiSummary = text.length() > 300 ? text.substring(0, 300) + "..." : text;
            }

            List<AiMatchResponse.RecommendedItem> items = calculateScoresAndRank(request, donations);

            // Enrich items with AI model insights if available
            if (!aiRankingsMap.isEmpty()) {
                for (AiMatchResponse.RecommendedItem item : items) {
                    if (aiRankingsMap.containsKey(item.getDonation().getId())) {
                        Map<String, Object> aiData = aiRankingsMap.get(item.getDonation().getId());
                        item.setMatchScore((Double) aiData.get("score"));
                        item.setMatchReason("🤖 Gemini AI: " + aiData.get("reason"));
                        item.setUrgencyLevel((String) aiData.get("urgency"));
                    }
                }
                items.sort(Comparator.comparingDouble(AiMatchResponse.RecommendedItem::getMatchScore).reversed());
            }

            return new AiMatchResponse(
                    "Gemini 1.5 Flash (Live Cloud AI)",
                    true,
                    aiSummary,
                    items
            );
        }

        throw new RuntimeException("Unexpected response status from Cloud AI");
    }

    private AiMatchResponse getHeuristicAiRecommendations(AiMatchRequest request, List<FoodDonationDto> donations) {
        List<AiMatchResponse.RecommendedItem> rankedItems = calculateScoresAndRank(request, donations);

        String rationale = String.format(
                "Evaluated %d active listings based on Multi-Factor AI Matching Model: Category Affinity, Location Proximity, Expiry Urgency, and Quantity Matching.",
                donations.size()
        );

        return new AiMatchResponse(
                "LeftoverLoop Smart Match Engine v1.0 (Local Heuristic AI)",
                false,
                rationale,
                rankedItems
        );
    }

    private List<AiMatchResponse.RecommendedItem> calculateScoresAndRank(AiMatchRequest request, List<FoodDonationDto> donations) {
        LocalDateTime now = LocalDateTime.now();
        List<AiMatchResponse.RecommendedItem> items = new ArrayList<>();

        for (FoodDonationDto d : donations) {
            double score = 50.0; // Base score
            List<String> reasons = new ArrayList<>();

            // Factor 1: Category Match (+25)
            if (request.getPreferredCategory() != null && !request.getPreferredCategory().isEmpty() && !"ALL".equalsIgnoreCase(request.getPreferredCategory())) {
                if (d.getCategory().equalsIgnoreCase(request.getPreferredCategory())) {
                    score += 25;
                    reasons.add("Exact Category Match (" + d.getCategory() + ")");
                }
            } else {
                score += 10;
            }

            // Factor 2: Location Proximity (+20)
            if (request.getLocation() != null && !request.getLocation().isEmpty()) {
                if (d.getLocation().toLowerCase().contains(request.getLocation().toLowerCase())) {
                    score += 20;
                    reasons.add("Location Proximity Match (" + d.getLocation() + ")");
                }
            }

            // Factor 3: Expiry Urgency (+20 max)
            if (d.getExpiryDate() != null) {
                long hoursLeft = Duration.between(now, d.getExpiryDate()).toHours();
                if (hoursLeft <= 3) {
                    score += 20;
                    reasons.add("Critical Expiry Urgency (" + hoursLeft + " hrs left)");
                } else if (hoursLeft <= 12) {
                    score += 15;
                    reasons.add("High Expiry Priority (" + hoursLeft + " hrs left)");
                } else if (hoursLeft <= 24) {
                    score += 10;
                    reasons.add("Standard Expiry Window (" + hoursLeft + " hrs left)");
                }
            }

            // Factor 4: Quantity Capacity (+15)
            if (request.getRequestedQuantity() != null && request.getRequestedQuantity() > 0) {
                if (d.getRemainingQuantity() >= request.getRequestedQuantity()) {
                    score += 15;
                    reasons.add("Full Quantity Fulfillment (" + d.getRemainingQuantity() + " " + d.getUnit() + " available)");
                } else {
                    score += 5;
                    reasons.add("Partial Quantity Fulfillment (" + d.getRemainingQuantity() + " " + d.getUnit() + " available)");
                }
            }

            // Factor 5: Receiver Priority (+10 for NGOs / Shelters)
            if ("SHELTER".equalsIgnoreCase(request.getReceiverType()) || "NGO".equalsIgnoreCase(request.getReceiverType())) {
                score += 10;
                reasons.add("Community / Bulk Receiver Priority Boost");
            }

            double finalScore = Math.min(99.5, Math.max(10.0, score));

            String urgencyLevel = "NORMAL";
            if (d.getExpiryDate() != null && Duration.between(now, d.getExpiryDate()).toHours() <= 4) {
                urgencyLevel = "HIGH";
            } else if (d.getExpiryDate() != null && Duration.between(now, d.getExpiryDate()).toHours() <= 12) {
                urgencyLevel = "MEDIUM";
            }

            String reasonText = String.join(" | ", reasons);
            if (reasonText.isEmpty()) {
                reasonText = "General Availability & High Freshness Score";
            }

            items.add(new AiMatchResponse.RecommendedItem(d, Math.round(finalScore * 10.0) / 10.0, reasonText, urgencyLevel));
        }

        // Sort descending by match score
        return items.stream()
                .sorted(Comparator.comparingDouble(AiMatchResponse.RecommendedItem::getMatchScore).reversed())
        .collect(Collectors.toList());
    }
}
