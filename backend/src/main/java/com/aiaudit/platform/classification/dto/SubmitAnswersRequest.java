package com.aiaudit.platform.classification.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Map;

@Data
public class SubmitAnswersRequest {

    @NotEmpty(message = "Answers are required")
    private Map<String, String> answers;
}
