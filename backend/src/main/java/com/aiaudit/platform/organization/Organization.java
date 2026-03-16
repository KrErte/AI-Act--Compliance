package com.aiaudit.platform.organization;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "organization")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String industry;

    private String country;

    @Column(name = "subscription_plan", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SubscriptionPlan subscriptionPlan = SubscriptionPlan.STARTER;

    @Column(name = "subscription_status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SubscriptionStatus subscriptionStatus = SubscriptionStatus.ACTIVE;

    @Column(name = "lemonsqueezy_customer_id")
    private String lemonsqueezyCustomerId;

    @Column(name = "lemonsqueezy_subscription_id")
    private String lemonsqueezySubscriptionId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
