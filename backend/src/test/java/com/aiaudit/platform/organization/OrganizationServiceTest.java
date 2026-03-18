package com.aiaudit.platform.organization;

import com.aiaudit.platform.TestDataBuilder;
import com.aiaudit.platform.common.exception.ResourceNotFoundException;
import com.aiaudit.platform.organization.dto.OrganizationDto;
import com.aiaudit.platform.organization.dto.UpdateOrganizationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @InjectMocks
    private OrganizationService service;

    private Organization organization;

    @BeforeEach
    void setUp() {
        organization = TestDataBuilder.anOrganization()
                .name("Test Corp")
                .industry("Technology")
                .country("EE")
                .build();
    }

    // ── getOrganization ──

    @Nested
    @DisplayName("getOrganization")
    class GetOrganizationTests {

        @Test
        @DisplayName("returns OrganizationDto for existing organization")
        void returnsOrganization() {
            UUID orgId = organization.getId();
            when(organizationRepository.findById(orgId)).thenReturn(Optional.of(organization));

            OrganizationDto result = service.getOrganization(orgId);

            assertNotNull(result);
            assertEquals(orgId, result.getId());
            assertEquals("Test Corp", result.getName());
            assertEquals("Technology", result.getIndustry());
            assertEquals("EE", result.getCountry());
            assertEquals("PROFESSIONAL", result.getSubscriptionPlan());
        }

        @Test
        @DisplayName("throws ResourceNotFoundException for non-existent organization")
        void throwsWhenNotFound() {
            UUID orgId = UUID.randomUUID();
            when(organizationRepository.findById(orgId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> service.getOrganization(orgId));
        }
    }

    // ── updateOrganization ──

    @Nested
    @DisplayName("updateOrganization")
    class UpdateOrganizationTests {

        @Test
        @DisplayName("updates name, industry, and country")
        void updatesOrganization() {
            UUID orgId = organization.getId();
            UpdateOrganizationRequest request = new UpdateOrganizationRequest();
            request.setName("Updated Corp");
            request.setIndustry("Healthcare");
            request.setCountry("DE");

            when(organizationRepository.findById(orgId)).thenReturn(Optional.of(organization));
            when(organizationRepository.save(any(Organization.class))).thenAnswer(inv -> inv.getArgument(0));

            OrganizationDto result = service.updateOrganization(orgId, request);

            assertNotNull(result);
            assertEquals("Updated Corp", result.getName());
            assertEquals("Healthcare", result.getIndustry());
            assertEquals("DE", result.getCountry());
        }

        @Test
        @DisplayName("throws ResourceNotFoundException for non-existent organization")
        void throwsWhenNotFound() {
            UUID orgId = UUID.randomUUID();
            UpdateOrganizationRequest request = new UpdateOrganizationRequest();
            request.setName("Name");

            when(organizationRepository.findById(orgId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> service.updateOrganization(orgId, request));
        }
    }
}
