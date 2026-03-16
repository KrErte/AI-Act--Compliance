package com.aiaudit.platform.subscription.dto;

import com.aiaudit.platform.organization.Organization;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubscriptionDto {
    private String plan;
    private String status;
    private String lemonsqueezySubscriptionId;

    public static SubscriptionDto from(Organization org) {
        return SubscriptionDto.builder()
                .plan(org.getSubscriptionPlan().name())
                .status(org.getSubscriptionStatus().name())
                .lemonsqueezySubscriptionId(org.getLemonsqueezySubscriptionId())
                .build();
    }
}
