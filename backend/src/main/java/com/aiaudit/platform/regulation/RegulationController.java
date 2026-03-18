package com.aiaudit.platform.regulation;

import com.aiaudit.platform.auth.AppUser;
import com.aiaudit.platform.common.ApiResponse;
import com.aiaudit.platform.regulation.dto.AssessmentResultDto;
import com.aiaudit.platform.regulation.dto.RegulationDetailDto;
import com.aiaudit.platform.regulation.dto.RegulationDto;
import com.aiaudit.platform.regulation.dto.SubmitAssessmentRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/regulations")
@RequiredArgsConstructor
public class RegulationController {

    private final RegulationService regulationService;
    private final RegulationAssessmentService assessmentService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RegulationDto>>> getAllRegulations() {
        return ResponseEntity.ok(ApiResponse.success(regulationService.getAllRegulations()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RegulationDetailDto>> getRegulation(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(regulationService.getRegulationDetail(id)));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<RegulationDetailDto>> getRegulationByCode(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.success(regulationService.getRegulationByCode(code)));
    }

    @PostMapping("/assessments")
    public ResponseEntity<ApiResponse<AssessmentResultDto>> submitAssessment(
            @Valid @RequestBody SubmitAssessmentRequest request,
            @AuthenticationPrincipal AppUser user) {
        var result = assessmentService.submitAssessment(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(result, "Assessment submitted"));
    }

    @GetMapping("/assessments/{regulationId}/latest")
    public ResponseEntity<ApiResponse<AssessmentResultDto>> getLatestAssessment(
            @PathVariable UUID regulationId,
            @AuthenticationPrincipal AppUser user) {
        return ResponseEntity.ok(ApiResponse.success(
                assessmentService.getLatestAssessment(regulationId, user.getOrganization().getId())));
    }

    @GetMapping("/assessments/history")
    public ResponseEntity<ApiResponse<List<AssessmentResultDto>>> getAssessmentHistory(
            @AuthenticationPrincipal AppUser user) {
        return ResponseEntity.ok(ApiResponse.success(
                assessmentService.getAssessmentHistory(user.getOrganization().getId())));
    }
}
