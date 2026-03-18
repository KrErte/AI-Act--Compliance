package com.aiaudit.platform.aisystem;

import com.aiaudit.platform.aisystem.dto.AiSystemDto;
import com.aiaudit.platform.aisystem.dto.CreateAiSystemRequest;
import com.aiaudit.platform.aisystem.dto.UpdateAiSystemRequest;
import com.aiaudit.platform.audit.AuditService;
import com.aiaudit.platform.auth.AppUser;
import com.aiaudit.platform.common.PagedResponse;
import com.aiaudit.platform.common.exception.ResourceNotFoundException;
import com.aiaudit.platform.organization.Organization;
import com.aiaudit.platform.organization.OrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.aiaudit.platform.TestDataBuilder.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiSystemServiceTest {

    @Mock
    private AiSystemRepository aiSystemRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private SubscriptionEnforcementService subscriptionEnforcement;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AiSystemService aiSystemService;

    private Organization organization;
    private AppUser user;
    private AiSystem aiSystem;
    private UUID orgId;

    @BeforeEach
    void setUp() {
        organization = anOrganization().build();
        orgId = organization.getId();
        user = anAppUser().organization(organization).build();
        aiSystem = anAiSystem().organization(organization).build();
    }

    // ────────────────────────────────────────────────────────────────────────────
    // listAiSystems
    // ────────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("listAiSystems")
    class ListAiSystems {

        @Test
        @DisplayName("should return paged response with no filters")
        void returnsPagedResponseNoFilters() {
            Page<AiSystem> page = new PageImpl<>(List.of(aiSystem));
            when(aiSystemRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

            PagedResponse<AiSystemDto> result = aiSystemService.listAiSystems(
                    orgId, null, null, null, null, null, 0, 10);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo(aiSystem.getName());
            verify(aiSystemRepository).findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        @DisplayName("should return paged response with search filter")
        void returnsPagedResponseWithSearch() {
            Page<AiSystem> page = new PageImpl<>(List.of(aiSystem));
            when(aiSystemRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

            PagedResponse<AiSystemDto> result = aiSystemService.listAiSystems(
                    orgId, "Test", null, null, null, null, 0, 10);

            assertThat(result.getContent()).hasSize(1);
            verify(aiSystemRepository).findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        @DisplayName("should return paged response with risk level filter")
        void returnsPagedResponseWithRiskLevelFilter() {
            Page<AiSystem> page = new PageImpl<>(List.of(aiSystem));
            when(aiSystemRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

            PagedResponse<AiSystemDto> result = aiSystemService.listAiSystems(
                    orgId, null, RiskLevel.HIGH, null, null, null, 0, 10);

            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("should return paged response with all filters")
        void returnsPagedResponseWithAllFilters() {
            Page<AiSystem> page = new PageImpl<>(List.of(aiSystem));
            when(aiSystemRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

            PagedResponse<AiSystemDto> result = aiSystemService.listAiSystems(
                    orgId, "Test", RiskLevel.HIGH, AiSystemStatus.ACTIVE,
                    ComplianceStatus.NOT_STARTED, DeploymentContext.INTERNAL, 0, 10);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getRiskLevel()).isEqualTo("HIGH");
        }

        @Test
        @DisplayName("should return empty page when no systems match")
        void returnsEmptyPage() {
            Page<AiSystem> page = new PageImpl<>(List.of());
            when(aiSystemRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

            PagedResponse<AiSystemDto> result = aiSystemService.listAiSystems(
                    orgId, "nonexistent", null, null, null, null, 0, 10);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
    }

    // ────────────────────────────────────────────────────────────────────────────
    // getAiSystem
    // ────────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getAiSystem")
    class GetAiSystem {

        @Test
        @DisplayName("should return DTO when system exists")
        void returnsDto() {
            when(aiSystemRepository.findByIdAndOrganizationIdAndDeletedFalse(aiSystem.getId(), orgId))
                    .thenReturn(Optional.of(aiSystem));

            AiSystemDto dto = aiSystemService.getAiSystem(aiSystem.getId(), orgId);

            assertThat(dto.getId()).isEqualTo(aiSystem.getId());
            assertThat(dto.getName()).isEqualTo(aiSystem.getName());
            assertThat(dto.getStatus()).isEqualTo(aiSystem.getStatus().name());
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when system not found")
        void throwsWhenNotFound() {
            UUID missingId = UUID.randomUUID();
            when(aiSystemRepository.findByIdAndOrganizationIdAndDeletedFalse(missingId, orgId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> aiSystemService.getAiSystem(missingId, orgId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("AI System");
        }
    }

    // ────────────────────────────────────────────────────────────────────────────
    // createAiSystem
    // ────────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("createAiSystem")
    class CreateAiSystem {

        @Test
        @DisplayName("should create system, log audit, and return DTO")
        void createsAndReturnsDto() {
            CreateAiSystemRequest request = new CreateAiSystemRequest();
            request.setName("New System");
            request.setDescription("Description");
            request.setVendor("Vendor Co");
            request.setVersion("2.0");
            request.setPurpose("Automated hiring");
            request.setDeploymentContext(DeploymentContext.CUSTOMER_FACING);
            request.setOrganizationRole(OrganizationRole.PROVIDER);

            when(organizationRepository.findById(orgId)).thenReturn(Optional.of(organization));
            when(aiSystemRepository.save(any(AiSystem.class))).thenAnswer(invocation -> {
                AiSystem saved = invocation.getArgument(0);
                saved.setId(UUID.randomUUID());
                return saved;
            });

            AiSystemDto dto = aiSystemService.createAiSystem(orgId, request, user);

            assertThat(dto.getName()).isEqualTo("New System");
            assertThat(dto.getDeploymentContext()).isEqualTo("CUSTOMER_FACING");
            verify(subscriptionEnforcement).checkCanCreateAiSystem(orgId);
            verify(aiSystemRepository).save(any(AiSystem.class));
            verify(auditService).log(eq("AI_SYSTEM"), any(UUID.class), eq("CREATED"),
                    isNull(), eq("New System"), isNull(), eq(user), eq(organization));
        }

        @Test
        @DisplayName("should enforce subscription limit before creating")
        void enforcesSubscriptionLimit() {
            CreateAiSystemRequest request = new CreateAiSystemRequest();
            request.setName("System");

            doThrow(new com.aiaudit.platform.common.exception.SubscriptionLimitException("limit reached"))
                    .when(subscriptionEnforcement).checkCanCreateAiSystem(orgId);

            assertThatThrownBy(() -> aiSystemService.createAiSystem(orgId, request, user))
                    .isInstanceOf(com.aiaudit.platform.common.exception.SubscriptionLimitException.class);

            verify(aiSystemRepository, never()).save(any());
        }
    }

    // ────────────────────────────────────────────────────────────────────────────
    // updateAiSystem
    // ────────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateAiSystem")
    class UpdateAiSystem {

        @Test
        @DisplayName("should update all fields and return DTO")
        void updatesFieldsAndReturnsDto() {
            when(aiSystemRepository.findByIdAndOrganizationIdAndDeletedFalse(aiSystem.getId(), orgId))
                    .thenReturn(Optional.of(aiSystem));
            when(aiSystemRepository.save(any(AiSystem.class))).thenAnswer(inv -> inv.getArgument(0));

            UpdateAiSystemRequest request = new UpdateAiSystemRequest();
            request.setName("Updated Name");
            request.setDescription("Updated desc");
            request.setVendor("Updated Vendor");
            request.setVersion("3.0");
            request.setPurpose("Updated purpose");
            request.setDeploymentContext(DeploymentContext.EMBEDDED);
            request.setOrganizationRole(OrganizationRole.BOTH);
            request.setStatus(AiSystemStatus.RETIRED);

            AiSystemDto dto = aiSystemService.updateAiSystem(aiSystem.getId(), orgId, request, user);

            assertThat(dto.getName()).isEqualTo("Updated Name");
            assertThat(dto.getStatus()).isEqualTo("RETIRED");
            assertThat(dto.getDeploymentContext()).isEqualTo("EMBEDDED");
            verify(auditService).log(eq("AI_SYSTEM"), eq(aiSystem.getId()), eq("UPDATED"),
                    anyString(), eq("Updated Name"), isNull(), eq(user), eq(organization));
        }

        @Test
        @DisplayName("should not change status when request status is null")
        void doesNotChangeStatusWhenNull() {
            AiSystem system = anAiSystem().organization(organization).status(AiSystemStatus.ACTIVE).build();
            when(aiSystemRepository.findByIdAndOrganizationIdAndDeletedFalse(system.getId(), orgId))
                    .thenReturn(Optional.of(system));
            when(aiSystemRepository.save(any(AiSystem.class))).thenAnswer(inv -> inv.getArgument(0));

            UpdateAiSystemRequest request = new UpdateAiSystemRequest();
            request.setName("Kept Status");
            request.setStatus(null);

            AiSystemDto dto = aiSystemService.updateAiSystem(system.getId(), orgId, request, user);

            assertThat(dto.getStatus()).isEqualTo("ACTIVE");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when system not found for update")
        void throwsWhenNotFoundForUpdate() {
            UUID missingId = UUID.randomUUID();
            when(aiSystemRepository.findByIdAndOrganizationIdAndDeletedFalse(missingId, orgId))
                    .thenReturn(Optional.empty());

            UpdateAiSystemRequest request = new UpdateAiSystemRequest();
            request.setName("X");

            assertThatThrownBy(() -> aiSystemService.updateAiSystem(missingId, orgId, request, user))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(aiSystemRepository, never()).save(any());
        }
    }

    // ────────────────────────────────────────────────────────────────────────────
    // deleteAiSystem
    // ────────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("deleteAiSystem")
    class DeleteAiSystem {

        @Test
        @DisplayName("should soft-delete system and log audit")
        void softDeletesAndLogsAudit() {
            when(aiSystemRepository.findByIdAndOrganizationIdAndDeletedFalse(aiSystem.getId(), orgId))
                    .thenReturn(Optional.of(aiSystem));
            when(aiSystemRepository.save(any(AiSystem.class))).thenAnswer(inv -> inv.getArgument(0));

            aiSystemService.deleteAiSystem(aiSystem.getId(), orgId, user);

            assertThat(aiSystem.isDeleted()).isTrue();
            verify(aiSystemRepository).save(aiSystem);
            verify(auditService).log(eq("AI_SYSTEM"), eq(aiSystem.getId()), eq("DELETED"),
                    eq(aiSystem.getName()), isNull(), isNull(), eq(user), eq(organization));
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when system not found for delete")
        void throwsWhenNotFoundForDelete() {
            UUID missingId = UUID.randomUUID();
            when(aiSystemRepository.findByIdAndOrganizationIdAndDeletedFalse(missingId, orgId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> aiSystemService.deleteAiSystem(missingId, orgId, user))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(aiSystemRepository, never()).save(any());
        }
    }

    // ────────────────────────────────────────────────────────────────────────────
    // findByIdAndOrg
    // ────────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("findByIdAndOrg")
    class FindByIdAndOrg {

        @Test
        @DisplayName("should return AiSystem when found")
        void returnsSystemWhenFound() {
            when(aiSystemRepository.findByIdAndOrganizationIdAndDeletedFalse(aiSystem.getId(), orgId))
                    .thenReturn(Optional.of(aiSystem));

            AiSystem result = aiSystemService.findByIdAndOrg(aiSystem.getId(), orgId);

            assertThat(result).isSameAs(aiSystem);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void throwsWhenNotFound() {
            UUID missingId = UUID.randomUUID();
            when(aiSystemRepository.findByIdAndOrganizationIdAndDeletedFalse(missingId, orgId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> aiSystemService.findByIdAndOrg(missingId, orgId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("AI System")
                    .hasMessageContaining(missingId.toString());
        }
    }
}
