package com.aiaudit.platform.subscription;

import com.aiaudit.platform.common.exception.ResourceNotFoundException;
import com.aiaudit.platform.organization.Organization;
import com.aiaudit.platform.organization.OrganizationRepository;
import com.aiaudit.platform.organization.SubscriptionPlan;
import com.aiaudit.platform.organization.SubscriptionStatus;
import com.aiaudit.platform.subscription.dto.SubscriptionDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {

    private final OrganizationRepository organizationRepository;

    @Transactional(readOnly = true)
    public SubscriptionDto getSubscription(UUID organizationId) {
        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", organizationId));
        return SubscriptionDto.from(org);
    }

    @Transactional
    public void handleWebhookEvent(String eventType, Map<String, Object> payload) {
        log.info("Processing LemonSqueezy webhook: {}", eventType);

        switch (eventType) {
            case "subscription_created" -> handleSubscriptionCreated(payload);
            case "subscription_updated" -> handleSubscriptionUpdated(payload);
            case "subscription_cancelled" -> handleSubscriptionCancelled(payload);
            case "subscription_payment_success" -> log.info("Payment success processed");
            case "subscription_payment_failed" -> handlePaymentFailed(payload);
            default -> log.warn("Unhandled webhook event: {}", eventType);
        }
    }

    private void handleSubscriptionCreated(Map<String, Object> payload) {
        String customerId = extractCustomerId(payload);
        String subscriptionId = extractSubscriptionId(payload);
        String planVariant = extractPlanVariant(payload);

        organizationRepository.findAll().stream()
                .filter(org -> customerId.equals(org.getLemonsqueezyCustomerId()))
                .findFirst()
                .ifPresent(org -> {
                    org.setLemonsqueezySubscriptionId(subscriptionId);
                    org.setSubscriptionPlan(mapPlan(planVariant));
                    org.setSubscriptionStatus(SubscriptionStatus.ACTIVE);
                    organizationRepository.save(org);
                    log.info("Subscription created for org: {}", org.getId());
                });
    }

    private void handleSubscriptionUpdated(Map<String, Object> payload) {
        String subscriptionId = extractSubscriptionId(payload);
        String planVariant = extractPlanVariant(payload);

        organizationRepository.findAll().stream()
                .filter(org -> subscriptionId.equals(org.getLemonsqueezySubscriptionId()))
                .findFirst()
                .ifPresent(org -> {
                    org.setSubscriptionPlan(mapPlan(planVariant));
                    organizationRepository.save(org);
                    log.info("Subscription updated for org: {}", org.getId());
                });
    }

    private void handleSubscriptionCancelled(Map<String, Object> payload) {
        String subscriptionId = extractSubscriptionId(payload);

        organizationRepository.findAll().stream()
                .filter(org -> subscriptionId.equals(org.getLemonsqueezySubscriptionId()))
                .findFirst()
                .ifPresent(org -> {
                    org.setSubscriptionStatus(SubscriptionStatus.CANCELLED);
                    organizationRepository.save(org);
                    log.info("Subscription cancelled for org: {}", org.getId());
                });
    }

    private void handlePaymentFailed(Map<String, Object> payload) {
        String subscriptionId = extractSubscriptionId(payload);

        organizationRepository.findAll().stream()
                .filter(org -> subscriptionId.equals(org.getLemonsqueezySubscriptionId()))
                .findFirst()
                .ifPresent(org -> {
                    org.setSubscriptionStatus(SubscriptionStatus.PAST_DUE);
                    organizationRepository.save(org);
                    log.info("Payment failed for org: {}", org.getId());
                });
    }

    @SuppressWarnings("unchecked")
    private String extractCustomerId(Map<String, Object> payload) {
        var data = (Map<String, Object>) payload.getOrDefault("data", Map.of());
        var attributes = (Map<String, Object>) data.getOrDefault("attributes", Map.of());
        return String.valueOf(attributes.getOrDefault("customer_id", ""));
    }

    @SuppressWarnings("unchecked")
    private String extractSubscriptionId(Map<String, Object> payload) {
        var data = (Map<String, Object>) payload.getOrDefault("data", Map.of());
        return String.valueOf(data.getOrDefault("id", ""));
    }

    @SuppressWarnings("unchecked")
    private String extractPlanVariant(Map<String, Object> payload) {
        var data = (Map<String, Object>) payload.getOrDefault("data", Map.of());
        var attributes = (Map<String, Object>) data.getOrDefault("attributes", Map.of());
        return String.valueOf(attributes.getOrDefault("variant_name", "starter"));
    }

    private SubscriptionPlan mapPlan(String variant) {
        if (variant == null) return SubscriptionPlan.STARTER;
        String lower = variant.toLowerCase();
        if (lower.contains("enterprise")) return SubscriptionPlan.ENTERPRISE;
        if (lower.contains("pro")) return SubscriptionPlan.PROFESSIONAL;
        return SubscriptionPlan.STARTER;
    }
}
