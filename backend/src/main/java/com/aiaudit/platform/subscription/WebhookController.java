package com.aiaudit.platform.subscription;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Map;

@RestController
@RequestMapping("/webhooks")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final SubscriptionService subscriptionService;
    private final ObjectMapper objectMapper;

    @Value("${lemonsqueezy.webhook-secret}")
    private String webhookSecret;

    @PostMapping("/lemonsqueezy")
    public ResponseEntity<Void> handleLemonSqueezyWebhook(
            @RequestHeader(value = "X-Signature", required = false) String signature,
            @RequestBody String rawBody) {

        // Verify HMAC signature
        if (webhookSecret != null && !webhookSecret.isBlank() && signature != null) {
            if (!verifySignature(rawBody, signature)) {
                log.warn("Invalid webhook signature");
                return ResponseEntity.status(401).build();
            }
        }

        try {
            Map<String, Object> payload = objectMapper.readValue(rawBody, new TypeReference<>() {});

            @SuppressWarnings("unchecked")
            Map<String, Object> meta = (Map<String, Object>) payload.getOrDefault("meta", Map.of());
            String eventType = String.valueOf(meta.getOrDefault("event_name", ""));

            subscriptionService.handleWebhookEvent(eventType, payload);
        } catch (Exception e) {
            log.error("Failed to parse webhook payload", e);
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok().build();
    }

    private boolean verifySignature(String payload, String signature) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                    webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String computed = HexFormat.of().formatHex(hash);
            return computed.equalsIgnoreCase(signature);
        } catch (Exception e) {
            log.error("Signature verification failed", e);
            return false;
        }
    }
}
