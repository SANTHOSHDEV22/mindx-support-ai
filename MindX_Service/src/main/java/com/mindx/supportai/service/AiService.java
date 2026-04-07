package com.mindx.supportai.service;

import com.mindx.supportai.entity.Message;
import com.mindx.supportai.entity.Sender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class AiService {

    @Value("${openai.api.key}")
    private String apiKey;

    private static final String URL = "https://api.openai.com/v1/chat/completions";

    public String getResponse(String userQuery, List<Message> history) {

        try {
            RestTemplate restTemplate = new RestTemplate();

            List<Map<String, String>> messages = new ArrayList<>();

            messages.add(Map.of(
                    "role", "system",
                    "content", """
                    You are an advanced customer support AI for an e-commerce platform.
                    
                    You MUST respond ONLY in JSON format:
                    
                    {
                      "reply": "main response",
                      "category": "order | refund | payment | support | general",
                      "suggestions": [
                        {"text": "Track order", "action": "track_order"},
                        {"text": "Cancel order", "action": "cancel_order"},
                        {"text": "Refund status", "action": "refund_status"},
                        {"text": "Talk to support", "action": "contact_support"}
                      ]
                    }
                    
                    Rules:
                    - Always be polite, human-like, and helpful
                    - Keep reply short but meaningful
                    - Detect intent category:
                      - order → tracking, delivery
                      - refund → refunds, cancellations
                      - payment → payment issues
                      - support → human help
                    - Suggestions must:
                      - be clickable actions
                      - max 6 suggestions
                      - short text (2–4 words)
                    - Avoid repeating same question
                    - If order ID is already given → use it
                    - Guide user step-by-step if needed
                    
                    Examples:
                    User: Where is my order?
                    → category: order
                    → suggestions: Track order, Delivery status, Cancel order
                    
                    User: I want refund
                    → category: refund
                    → suggestions: Refund status, Cancel order, Talk to support
                    """
            ));

            if (history != null) {
                for (Message msg : history) {

                    String role = (msg.getSender() == Sender.USER)
                            ? "user"
                            : "assistant";

                    messages.add(Map.of(
                            "role", role,
                            "content", msg.getMessage()
                    ));
                }
            }

            messages.add(Map.of(
                    "role", "user",
                    "content", userQuery
            ));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-4o-mini");
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> entity =
                    new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    URL,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            List<Map<String, Object>> choices =
                    (List<Map<String, Object>>) response.getBody().get("choices");

            if (choices == null || choices.isEmpty()) {
                return fallbackJson(userQuery);
            }

            Map<String, Object> message =
                    (Map<String, Object>) choices.get(0).get("message");

            String content = message.get("content") != null
                    ? message.get("content").toString()
                    : fallbackJson(userQuery);

            return content.trim();

        } catch (Exception e) {
            System.out.println("AI ERROR: " + e.getMessage());
            return fallbackJson(userQuery);
        }
    }

    private String fallbackJson(String query) {

        query = query.toLowerCase();

        if (query.contains("order")) {
            return """
            {
              "reply": "Sure! Please provide your order ID so I can check the status for you.",
              "category": "order",
              "suggestions": [
                {"text": "Track order", "action": "track_order"},
                {"text": "Delivery status", "action": "delivery_status"},
                {"text": "Cancel order", "action": "cancel_order"}
              ]
            }
            """;
        }

        if (query.contains("refund")) {
            return """
            {
              "reply": "I understand you'd like a refund. Please share your order ID so I can assist.",
              "category": "refund",
              "suggestions": [
                {"text": "Refund status", "action": "refund_status"},
                {"text": "Cancel order", "action": "cancel_order"},
                {"text": "Talk to support", "action": "contact_support"}
              ]
            }
            """;
        }

        return """
            {
              "reply": "I'm here to help. Could you tell me more about your issue?",
              "category": "general",
              "suggestions": [
                {"text": "Track order", "action": "track_order"},
                {"text": "Refund request", "action": "refund"},
                {"text": "Payment issue", "action": "payment_issue"},
                {"text": "Contact support", "action": "contact_support"}
              ]
            }
            """;
    }
}