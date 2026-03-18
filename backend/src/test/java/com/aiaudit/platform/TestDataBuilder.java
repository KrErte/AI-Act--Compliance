package com.aiaudit.platform;

import com.aiaudit.platform.aisystem.*;
import com.aiaudit.platform.alert.AlertSeverity;
import com.aiaudit.platform.alert.AlertType;
import com.aiaudit.platform.alert.ComplianceAlert;
import com.aiaudit.platform.audit.AuditLog;
import com.aiaudit.platform.auth.AppUser;
import com.aiaudit.platform.auth.RefreshToken;
import com.aiaudit.platform.auth.UserRole;
import com.aiaudit.platform.compliance.ComplianceObligation;
import com.aiaudit.platform.compliance.ObligationStatus;
import com.aiaudit.platform.document.DocumentStatus;
import com.aiaudit.platform.document.DocumentType;
import com.aiaudit.platform.document.GeneratedDocument;
import com.aiaudit.platform.gpai.GpaiModel;
import com.aiaudit.platform.gpai.GpaiModelType;
import com.aiaudit.platform.organization.Organization;
import com.aiaudit.platform.organization.SubscriptionPlan;
import com.aiaudit.platform.organization.SubscriptionStatus;
import com.aiaudit.platform.team.TeamInvitation;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

/**
 * Factory methods for creating test entities with sensible defaults.
 * All builders can be customized before calling build().
 */
public final class TestDataBuilder {

    private TestDataBuilder() {}

    // ── Organization ──

    public static Organization.OrganizationBuilder anOrganization() {
        return Organization.builder()
                .id(UUID.randomUUID())
                .name("Test Organization")
                .industry("Technology")
                .country("EE")
                .subscriptionPlan(SubscriptionPlan.PROFESSIONAL)
                .subscriptionStatus(SubscriptionStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now());
    }

    // ── AppUser ──

    public static AppUser.AppUserBuilder anAppUser() {
        return AppUser.builder()
                .id(UUID.randomUUID())
                .email("user@test.com")
                .passwordHash("$2a$10$hashedpassword")
                .firstName("Test")
                .lastName("User")
                .role(UserRole.ADMIN)
                .organization(anOrganization().build())
                .enabled(true)
                .languagePreference("en")
                .createdAt(Instant.now())
                .updatedAt(Instant.now());
    }

    public static AppUser.AppUserBuilder anOwner() {
        return anAppUser()
                .role(UserRole.OWNER)
                .email("owner@test.com")
                .firstName("Owner");
    }

    public static AppUser.AppUserBuilder aViewer() {
        return anAppUser()
                .role(UserRole.VIEWER)
                .email("viewer@test.com")
                .firstName("Viewer");
    }

    // ── AiSystem ──

    public static AiSystem.AiSystemBuilder anAiSystem() {
        return AiSystem.builder()
                .id(UUID.randomUUID())
                .name("Test AI System")
                .description("A test AI system for unit testing")
                .vendor("Test Vendor")
                .version("1.0")
                .purpose("Testing purposes")
                .deploymentContext(DeploymentContext.INTERNAL)
                .organizationRole(OrganizationRole.DEPLOYER)
                .status(AiSystemStatus.ACTIVE)
                .riskLevel(RiskLevel.HIGH)
                .complianceScore(0)
                .complianceStatus(ComplianceStatus.NOT_STARTED)
                .organization(anOrganization().build())
                .deleted(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now());
    }

    // ── RefreshToken ──

    public static RefreshToken.RefreshTokenBuilder aRefreshToken() {
        return RefreshToken.builder()
                .id(UUID.randomUUID())
                .token(UUID.randomUUID().toString())
                .user(anAppUser().build())
                .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .revoked(false)
                .createdAt(Instant.now());
    }

    // ── ComplianceObligation ──

    public static ComplianceObligation.ComplianceObligationBuilder anObligation() {
        return ComplianceObligation.builder()
                .id(UUID.randomUUID())
                .aiSystem(anAiSystem().build())
                .articleRef("Art. 9")
                .articleTitle("Risk Management System")
                .description("Establish and maintain a risk management system")
                .status(ObligationStatus.NOT_STARTED)
                .dueDate(LocalDate.now().plusMonths(3))
                .sortOrder(0)
                .createdAt(Instant.now())
                .updatedAt(Instant.now());
    }

    // ── GpaiModel ──

    public static GpaiModel.GpaiModelBuilder aGpaiModel() {
        return GpaiModel.builder()
                .id(UUID.randomUUID())
                .name("Test GPAI Model")
                .provider("Test Provider")
                .modelType(GpaiModelType.GENERAL)
                .hasSystemicRisk(false)
                .trainingComputeFlops(new BigDecimal("1E25"))
                .description("A test GPAI model")
                .version("1.0")
                .openSource(false)
                .organization(anOrganization().build())
                .createdAt(Instant.now())
                .updatedAt(Instant.now());
    }

    // ── GeneratedDocument ──

    public static GeneratedDocument.GeneratedDocumentBuilder aDocument() {
        return GeneratedDocument.builder()
                .id(UUID.randomUUID())
                .aiSystem(anAiSystem().build())
                .documentType(DocumentType.FRIA)
                .title("Test Document")
                .content("# Test Content\n\nThis is a test document.")
                .format("MARKDOWN")
                .status(DocumentStatus.COMPLETED)
                .version(1)
                .metadata(Map.of("generated", true))
                .createdAt(Instant.now())
                .updatedAt(Instant.now());
    }

    // ── ComplianceAlert ──

    public static ComplianceAlert.ComplianceAlertBuilder anAlert() {
        return ComplianceAlert.builder()
                .id(UUID.randomUUID())
                .organization(anOrganization().build())
                .aiSystem(anAiSystem().build())
                .alertType(AlertType.COMPLIANCE)
                .title("Test Alert")
                .message("This is a test compliance alert")
                .severity(AlertSeverity.MEDIUM)
                .read(false)
                .dismissed(false)
                .emailSent(false)
                .createdAt(Instant.now());
    }

    // ── TeamInvitation ──

    public static TeamInvitation.TeamInvitationBuilder anInvitation() {
        return TeamInvitation.builder()
                .id(UUID.randomUUID())
                .email("invited@test.com")
                .role(UserRole.VIEWER)
                .token(UUID.randomUUID().toString())
                .organization(anOrganization().build())
                .invitedBy(anAppUser().build())
                .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .createdAt(Instant.now());
    }

    // ── AuditLog ──

    public static AuditLog.AuditLogBuilder anAuditLog() {
        return AuditLog.builder()
                .id(UUID.randomUUID())
                .entityType("AiSystem")
                .entityId(UUID.randomUUID())
                .action("CREATE")
                .newValue("{\"name\":\"Test\"}")
                .organization(anOrganization().build())
                .user(anAppUser().build())
                .createdAt(Instant.now());
    }
}
