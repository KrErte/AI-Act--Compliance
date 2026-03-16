package com.aiaudit.platform.organization.dto;

import com.aiaudit.platform.organization.Organization;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class OrganizationDto {
    private UUID id;
    private String name;
    private String industry;
    private String country;
    private String subscriptionPlan;
    private String subscriptionStatus;

    public static OrganizationDto from(Organization org) {
        return OrganizationDto.builder()
                .id(org.getId())
                .name(org.getName())
                .industry(org.getIndustry())
                .country(org.getCountry())
                .subscriptionPlan(org.getSubscriptionPlan().name())
                .subscriptionStatus(org.getSubscriptionStatus().name())
                .build();
    }
}
