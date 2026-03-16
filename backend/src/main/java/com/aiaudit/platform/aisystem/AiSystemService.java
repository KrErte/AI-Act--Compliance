package com.aiaudit.platform.aisystem;

import com.aiaudit.platform.aisystem.dto.*;
import com.aiaudit.platform.audit.AuditService;
import com.aiaudit.platform.auth.AppUser;
import com.aiaudit.platform.common.PagedResponse;
import com.aiaudit.platform.common.exception.ResourceNotFoundException;
import com.aiaudit.platform.organization.Organization;
import com.aiaudit.platform.organization.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AiSystemService {

    private final AiSystemRepository aiSystemRepository;
    private final OrganizationRepository organizationRepository;
    private final SubscriptionEnforcementService subscriptionEnforcement;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public PagedResponse<AiSystemDto> listAiSystems(UUID organizationId, String search,
                                                     RiskLevel riskLevel, AiSystemStatus status,
                                                     ComplianceStatus complianceStatus,
                                                     DeploymentContext deploymentContext,
                                                     int page, int size) {
        Specification<AiSystem> spec = Specification
                .where(AiSystemSpecifications.belongsToOrganization(organizationId))
                .and(AiSystemSpecifications.notDeleted())
                .and(AiSystemSpecifications.nameContains(search))
                .and(AiSystemSpecifications.hasRiskLevel(riskLevel))
                .and(AiSystemSpecifications.hasStatus(status))
                .and(AiSystemSpecifications.hasComplianceStatus(complianceStatus))
                .and(AiSystemSpecifications.hasDeploymentContext(deploymentContext));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AiSystemDto> result = aiSystemRepository.findAll(spec, pageable).map(AiSystemDto::from);
        return PagedResponse.from(result);
    }

    @Transactional(readOnly = true)
    public AiSystemDto getAiSystem(UUID id, UUID organizationId) {
        AiSystem system = findByIdAndOrg(id, organizationId);
        return AiSystemDto.from(system);
    }

    @Transactional
    public AiSystemDto createAiSystem(UUID organizationId, CreateAiSystemRequest request, AppUser user) {
        subscriptionEnforcement.checkCanCreateAiSystem(organizationId);

        Organization org = organizationRepository.findById(organizationId).orElseThrow();

        AiSystem system = AiSystem.builder()
                .name(request.getName())
                .description(request.getDescription())
                .vendor(request.getVendor())
                .version(request.getVersion())
                .purpose(request.getPurpose())
                .deploymentContext(request.getDeploymentContext())
                .organizationRole(request.getOrganizationRole())
                .organization(org)
                .build();

        system = aiSystemRepository.save(system);
        auditService.log("AI_SYSTEM", system.getId(), "CREATED", null, system.getName(), null, user, org);
        return AiSystemDto.from(system);
    }

    @Transactional
    public AiSystemDto updateAiSystem(UUID id, UUID organizationId, UpdateAiSystemRequest request, AppUser user) {
        AiSystem system = findByIdAndOrg(id, organizationId);
        String oldName = system.getName();

        system.setName(request.getName());
        system.setDescription(request.getDescription());
        system.setVendor(request.getVendor());
        system.setVersion(request.getVersion());
        system.setPurpose(request.getPurpose());
        system.setDeploymentContext(request.getDeploymentContext());
        system.setOrganizationRole(request.getOrganizationRole());
        if (request.getStatus() != null) {
            system.setStatus(request.getStatus());
        }

        system = aiSystemRepository.save(system);
        auditService.log("AI_SYSTEM", system.getId(), "UPDATED", oldName, system.getName(), null, user, system.getOrganization());
        return AiSystemDto.from(system);
    }

    @Transactional
    public void deleteAiSystem(UUID id, UUID organizationId, AppUser user) {
        AiSystem system = findByIdAndOrg(id, organizationId);
        system.setDeleted(true);
        aiSystemRepository.save(system);
        auditService.log("AI_SYSTEM", system.getId(), "DELETED", system.getName(), null, null, user, system.getOrganization());
    }

    public AiSystem findByIdAndOrg(UUID id, UUID organizationId) {
        return aiSystemRepository.findByIdAndOrganizationIdAndDeletedFalse(id, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("AI System", "id", id));
    }
}
