package com.aiaudit.platform.aisystem;

import com.aiaudit.platform.common.exception.SubscriptionLimitException;
import com.aiaudit.platform.organization.Organization;
import com.aiaudit.platform.organization.OrganizationRepository;
import com.aiaudit.platform.organization.SubscriptionPlan;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubscriptionEnforcementService {

    private final AiSystemRepository aiSystemRepository;
    private final OrganizationRepository organizationRepository;

    @Value("${subscription.plans.STARTER.max-ai-systems}")
    private int starterMaxSystems;

    @Value("${subscription.plans.PROFESSIONAL.max-ai-systems}")
    private int proMaxSystems;

    @Value("${subscription.plans.ENTERPRISE.max-ai-systems}")
    private int enterpriseMaxSystems;

    public void checkCanCreateAiSystem(UUID organizationId) {
        Organization org = organizationRepository.findById(organizationId).orElseThrow();
        long currentCount = aiSystemRepository.countByOrganizationIdAndDeletedFalse(organizationId);
        int limit = getLimit(org.getSubscriptionPlan());

        if (limit >= 0 && currentCount >= limit) {
            throw new SubscriptionLimitException(
                    String.format("AI system limit reached (%d/%d). Upgrade your plan to add more.", currentCount, limit));
        }
    }

    private int getLimit(SubscriptionPlan plan) {
        return switch (plan) {
            case STARTER -> starterMaxSystems;
            case PROFESSIONAL -> proMaxSystems;
            case ENTERPRISE -> enterpriseMaxSystems;
        };
    }
}
