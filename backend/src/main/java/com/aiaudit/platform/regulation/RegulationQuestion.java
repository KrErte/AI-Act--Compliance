package com.aiaudit.platform.regulation;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "regulation_question")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegulationQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_id", nullable = false)
    private RegulationDomain domain;

    @Column(name = "question_en", nullable = false, columnDefinition = "TEXT")
    private String questionEn;

    @Column(name = "question_et", columnDefinition = "TEXT")
    private String questionEt;

    @Column(name = "article_ref", length = 100)
    private String articleRef;

    @Column(name = "explanation_en", columnDefinition = "TEXT")
    private String explanationEn;

    @Column(name = "explanation_et", columnDefinition = "TEXT")
    private String explanationEt;

    @Column(name = "recommendation_en", columnDefinition = "TEXT")
    private String recommendationEn;

    @Column(name = "recommendation_et", columnDefinition = "TEXT")
    private String recommendationEt;

    @Column(name = "sort_order")
    @Builder.Default
    private int sortOrder = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
