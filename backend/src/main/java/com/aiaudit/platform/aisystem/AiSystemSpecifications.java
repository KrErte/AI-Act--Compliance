package com.aiaudit.platform.aisystem;

import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public final class AiSystemSpecifications {

    private AiSystemSpecifications() {}

    public static Specification<AiSystem> belongsToOrganization(UUID organizationId) {
        return (root, query, cb) -> cb.equal(root.get("organization").get("id"), organizationId);
    }

    public static Specification<AiSystem> notDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get("deleted"));
    }

    public static Specification<AiSystem> hasRiskLevel(RiskLevel riskLevel) {
        return (root, query, cb) -> riskLevel == null ? null : cb.equal(root.get("riskLevel"), riskLevel);
    }

    public static Specification<AiSystem> hasStatus(AiSystemStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<AiSystem> hasComplianceStatus(ComplianceStatus complianceStatus) {
        return (root, query, cb) -> complianceStatus == null ? null : cb.equal(root.get("complianceStatus"), complianceStatus);
    }

    public static Specification<AiSystem> hasDeploymentContext(DeploymentContext deploymentContext) {
        return (root, query, cb) -> deploymentContext == null ? null : cb.equal(root.get("deploymentContext"), deploymentContext);
    }

    public static Specification<AiSystem> nameContains(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) return null;
            return cb.like(cb.lower(root.get("name")), "%" + search.toLowerCase() + "%");
        };
    }
}
