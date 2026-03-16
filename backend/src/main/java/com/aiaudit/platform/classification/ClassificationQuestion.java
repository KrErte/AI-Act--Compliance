package com.aiaudit.platform.classification;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "classification_question")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassificationQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "question_key", nullable = false, unique = true)
    private String questionKey;

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "question_type", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private QuestionType questionType = QuestionType.YES_NO;

    @Column(columnDefinition = "jsonb")
    private String options;

    @Column(nullable = false)
    private String category;

    @Column(name = "help_text", columnDefinition = "TEXT")
    private String helpText;

    @Column(name = "sort_order")
    private int sortOrder;

    @Column(name = "depends_on")
    private String dependsOn;

    @Column(name = "depends_on_answer")
    private String dependsOnAnswer;

    @Builder.Default
    private boolean active = true;
}
