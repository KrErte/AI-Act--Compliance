package com.aiaudit.platform.admin;

import com.aiaudit.platform.admin.dto.AdminStatsDto;
import com.aiaudit.platform.admin.dto.AdminUserDto;
import com.aiaudit.platform.admin.dto.UpdateUserRequest;
import com.aiaudit.platform.aisystem.AiSystemRepository;
import com.aiaudit.platform.aisystem.RiskLevel;
import com.aiaudit.platform.audit.AuditService;
import com.aiaudit.platform.audit.dto.AuditLogDto;
import com.aiaudit.platform.auth.AppUser;
import com.aiaudit.platform.auth.UserRepository;
import com.aiaudit.platform.auth.UserRole;
import com.aiaudit.platform.common.PagedResponse;
import com.aiaudit.platform.common.exception.BadRequestException;
import com.aiaudit.platform.common.exception.ForbiddenException;
import com.aiaudit.platform.common.exception.ResourceNotFoundException;
import com.aiaudit.platform.compliance.ComplianceObligationRepository;
import com.aiaudit.platform.compliance.ObligationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final AiSystemRepository aiSystemRepository;
    private final ComplianceObligationRepository obligationRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public PagedResponse<AdminUserDto> getUsers(UUID organizationId, int page, int size) {
        Page<AdminUserDto> result = userRepository
                .findByOrganizationIdOrderByCreatedAtDesc(organizationId, PageRequest.of(page, size))
                .map(AdminUserDto::from);
        return PagedResponse.from(result);
    }

    @Transactional
    public AdminUserDto updateUser(UUID userId, UpdateUserRequest request, AppUser admin) {
        AppUser target = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!target.getOrganization().getId().equals(admin.getOrganization().getId())) {
            throw new ForbiddenException("Cannot modify users from another organization");
        }

        if (target.getId().equals(admin.getId())) {
            throw new BadRequestException("Cannot modify your own account via admin panel");
        }

        // Only OWNER can change roles to OWNER or modify another OWNER
        if (target.getRole() == UserRole.OWNER && admin.getRole() != UserRole.OWNER) {
            throw new ForbiddenException("Only owners can modify other owners");
        }
        if (request.getRole() == UserRole.OWNER && admin.getRole() != UserRole.OWNER) {
            throw new ForbiddenException("Only owners can assign the owner role");
        }

        String oldRole = target.getRole().name();
        boolean oldEnabled = target.isEnabled();

        if (request.getRole() != null) {
            target.setRole(request.getRole());
        }
        if (request.getEnabled() != null) {
            target.setEnabled(request.getEnabled());
        }

        target = userRepository.save(target);

        auditService.log("AppUser", target.getId(), "UPDATED",
                "role=" + oldRole + ",enabled=" + oldEnabled,
                "role=" + target.getRole().name() + ",enabled=" + target.isEnabled(),
                null, admin, admin.getOrganization());

        return AdminUserDto.from(target);
    }

    @Transactional
    public void deactivateUser(UUID userId, AppUser admin) {
        AppUser target = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!target.getOrganization().getId().equals(admin.getOrganization().getId())) {
            throw new ForbiddenException("Cannot modify users from another organization");
        }

        if (target.getId().equals(admin.getId())) {
            throw new BadRequestException("Cannot deactivate your own account");
        }

        if (target.getRole() == UserRole.OWNER && admin.getRole() != UserRole.OWNER) {
            throw new ForbiddenException("Only owners can deactivate other owners");
        }

        target.setEnabled(false);
        userRepository.save(target);

        auditService.log("AppUser", target.getId(), "DEACTIVATED",
                null, null, null, admin, admin.getOrganization());
    }

    @Transactional(readOnly = true)
    public AdminStatsDto getStats(UUID organizationId) {
        long totalUsers = userRepository.countByOrganizationId(organizationId);
        long totalSystems = aiSystemRepository.countByOrganizationIdAndDeletedFalse(organizationId);

        Map<String, Long> riskDistribution = new LinkedHashMap<>();
        for (RiskLevel level : RiskLevel.values()) {
            riskDistribution.put(level.name(), aiSystemRepository.countByOrganizationIdAndRiskLevel(organizationId, level));
        }

        Map<String, Long> usersByRole = new LinkedHashMap<>();
        for (UserRole role : UserRole.values()) {
            usersByRole.put(role.name(), userRepository.countByOrganizationIdAndRole(organizationId, role));
        }

        var obligations = obligationRepository.findUpcomingByOrganizationId(organizationId);
        long totalObligations = obligations.size();
        long completed = obligations.stream().filter(o -> o.getStatus() == ObligationStatus.COMPLETED).count();
        int score = totalObligations > 0 ? (int) ((completed * 100) / totalObligations) : 0;

        return AdminStatsDto.builder()
                .totalUsers(totalUsers)
                .totalAiSystems(totalSystems)
                .overallComplianceScore(score)
                .totalObligations(totalObligations)
                .completedObligations(completed)
                .riskDistribution(riskDistribution)
                .usersByRole(usersByRole)
                .build();
    }

    @Transactional(readOnly = true)
    public PagedResponse<AuditLogDto> getAuditLog(UUID organizationId, int page, int size) {
        return auditService.getAuditLog(organizationId, page, size);
    }
}
