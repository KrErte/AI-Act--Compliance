package com.aiaudit.platform.regulation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class SubmitAssessmentRequest {

    @NotNull
    private UUID regulationId;

    @NotNull
    private List<AnswerInput> answers;

    @Data
    public static class AnswerInput {
        @NotNull
        private UUID questionId;
        private int answer; // 0-4
        private String notes;
    }
}
