package com.aiaudit.platform.aisystem;

import com.aiaudit.platform.auth.AppUser;
import com.aiaudit.platform.organization.Organization;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ai_system")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiSystem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String vendor;
    private String version;

    @Column(columnDefinition = "TEXT")
    private String purpose;

    @Column(name = "deployment_context")
    @Enumerated(EnumType.STRING)
    private DeploymentContext deploymentContext;

    @Column(name = "organization_role")
    @Enumerated(EnumType.STRING)
    private OrganizationRole organizationRole;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AiSystemStatus status = AiSystemStatus.DRAFT;

    @Column(name = "risk_level")
    @Enumerated(EnumType.STRING)
    private RiskLevel riskLevel;

    @Column(name = "compliance_score")
    @Builder.Default
    private Integer complianceScore = 0;

    @Column(name = "compliance_status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ComplianceStatus complianceStatus = ComplianceStatus.NOT_STARTED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(name = "classified_at")
    private Instant classifiedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classified_by")
    private AppUser classifiedBy;

    @Builder.Default
    private boolean deleted = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
