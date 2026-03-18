package com.aiaudit.platform.aisystem;

import com.aiaudit.platform.common.exception.SubscriptionLimitException;
import com.aiaudit.platform.organization.Organization;
import com.aiaudit.platform.organization.OrganizationRepository;
import com.aiaudit.platform.organization.SubscriptionPlan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static com.aiaudit.platform.TestDataBuilder.anOrganization;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionEnforcementServiceTest {

    @Mock
    private AiSystemRepository aiSystemRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @InjectMocks
    private SubscriptionEnforcementService subscriptionEnforcementService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(subscriptionEnforcementService, "starterMaxSystems", 3);
        ReflectionTestUtils.setField(subscriptionEnforcementService, "proMaxSystems", 15);
        ReflectionTestUtils.setField(subscriptionEnforcementService, "enterpriseMaxSystems", -1);
    }

    @Test
    @DisplayName("STARTER plan under limit - should not throw")
    void starterPlanUnderLimit() {
        Organization org = anOrganization().subscriptionPlan(SubscriptionPlan.STARTER).build();
        UUID orgId = org.getId();

        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(org));
        when(aiSystemRepository.countByOrganizationIdAndDeletedFalse(orgId)).thenReturn(2L);

        assertThatCode(() -> subscriptionEnforcementService.checkCanCreateAiSystem(orgId))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("STARTER plan at limit - should throw SubscriptionLimitException")
    void starterPlanAtLimit() {
        Organization org = anOrganization().subscriptionPlan(SubscriptionPlan.STARTER).build();
        UUID orgId = org.getId();

        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(org));
        when(aiSystemRepository.countByOrganizationIdAndDeletedFalse(orgId)).thenReturn(3L);

        assertThatThrownBy(() -> subscriptionEnforcementService.checkCanCreateAiSystem(orgId))
                .isInstanceOf(SubscriptionLimitException.class)
                .hasMessageContaining("AI system limit reached")
                .hasMessageContaining("3/3");
    }

    @Test
    @DisplayName("PROFESSIONAL plan at limit - should throw SubscriptionLimitException")
    void professionalPlanAtLimit() {
        Organization org = anOrganization().subscriptionPlan(SubscriptionPlan.PROFESSIONAL).build();
        UUID orgId = org.getId();

        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(org));
        when(aiSystemRepository.countByOrganizationIdAndDeletedFalse(orgId)).thenReturn(15L);

        assertThatThrownBy(() -> subscriptionEnforcementService.checkCanCreateAiSystem(orgId))
                .isInstanceOf(SubscriptionLimitException.class)
                .hasMessageContaining("15/15");
    }

    @Test
    @DisplayName("ENTERPRISE plan with high count - should not throw (unlimited)")
    void enterprisePlanUnlimited() {
        Organization org = anOrganization().subscriptionPlan(SubscriptionPlan.ENTERPRISE).build();
        UUID orgId = org.getId();

        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(org));
        when(aiSystemRepository.countByOrganizationIdAndDeletedFalse(orgId)).thenReturn(999L);

        assertThatCode(() -> subscriptionEnforcementService.checkCanCreateAiSystem(orgId))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Organization not found - should throw NoSuchElementException")
    void organizationNotFound() {
        UUID unknownOrgId = UUID.randomUUID();
        when(organizationRepository.findById(unknownOrgId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subscriptionEnforcementService.checkCanCreateAiSystem(unknownOrgId))
                .isInstanceOf(NoSuchElementException.class);
    }
}
