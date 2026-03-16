package com.aiaudit.platform.gpai;

import com.aiaudit.platform.organization.Organization;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "gpai_model")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GpaiModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String provider;

    @Column(name = "model_type", length = 100)
    @Enumerated(EnumType.STRING)
    private GpaiModelType modelType;

    @Column(name = "has_systemic_risk")
    @Builder.Default
    private boolean hasSystemicRisk = false;

    @Column(name = "training_compute_flops")
    private BigDecimal trainingComputeFlops;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 100)
    private String version;

    @Column(name = "open_source")
    @Builder.Default
    private boolean openSource = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
