package com.aiaudit.platform.organization;

import com.aiaudit.platform.common.exception.ResourceNotFoundException;
import com.aiaudit.platform.organization.dto.OrganizationDto;
import com.aiaudit.platform.organization.dto.UpdateOrganizationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    @Transactional(readOnly = true)
    public OrganizationDto getOrganization(UUID organizationId) {
        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", organizationId));
        return OrganizationDto.from(org);
    }

    @Transactional
    public OrganizationDto updateOrganization(UUID organizationId, UpdateOrganizationRequest request) {
        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", organizationId));

        org.setName(request.getName());
        org.setIndustry(request.getIndustry());
        org.setCountry(request.getCountry());

        return OrganizationDto.from(organizationRepository.save(org));
    }
}
