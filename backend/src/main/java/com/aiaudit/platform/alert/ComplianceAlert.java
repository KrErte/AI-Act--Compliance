package com.aiaudit.platform.alert;

import com.aiaudit.platform.aisystem.AiSystem;
import com.aiaudit.platform.organization.Organization;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "compliance_alert")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplianceAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ai_system_id")
    private AiSystem aiSystem;

    @Column(name = "alert_type", nullable = false, length = 100)
    @Enumerated(EnumType.STRING)
    private AlertType alertType;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AlertSeverity severity = AlertSeverity.MEDIUM;

    @Builder.Default
    private boolean read = false;

    @Column(length = 100)
    private String source;

    @Column(name = "link_url", length = 500)
    private String linkUrl;

    @Builder.Default
    private boolean dismissed = false;

    @Column(name = "email_sent")
    @Builder.Default
    private boolean emailSent = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
