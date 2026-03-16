package com.aiaudit.platform.classification;

import com.aiaudit.platform.auth.AppUser;
import com.aiaudit.platform.classification.dto.*;
import com.aiaudit.platform.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ClassificationController {

    private final ClassificationService classificationService;

    @GetMapping("/ai-systems/{aiSystemId}/classification/questions")
    public ResponseEntity<ApiResponse<List<QuestionDto>>> getQuestions() {
        List<QuestionDto> questions = classificationService.getQuestions();
        return ResponseEntity.ok(ApiResponse.success(questions));
    }

    @PostMapping("/ai-systems/{aiSystemId}/classification/run")
    public ResponseEntity<ApiResponse<ClassificationResult>> classifyAiSystem(
            @AuthenticationPrincipal AppUser user,
            @PathVariable UUID aiSystemId,
            @Valid @RequestBody SubmitAnswersRequest request) {

        ClassificationResult result = classificationService.classifyAiSystem(
                aiSystemId, user.getOrganization().getId(), request.getAnswers(), user);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // Public endpoint — no auth, no persistence
    @GetMapping("/public/classify/questions")
    public ResponseEntity<ApiResponse<List<QuestionDto>>> getPublicQuestions() {
        List<QuestionDto> questions = classificationService.getQuestions();
        return ResponseEntity.ok(ApiResponse.success(questions));
    }

    @PostMapping("/public/classify")
    public ResponseEntity<ApiResponse<ClassificationResult>> classifyPublic(
            @Valid @RequestBody SubmitAnswersRequest request) {
        ClassificationResult result = classificationService.classifyPublic(request.getAnswers());
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
