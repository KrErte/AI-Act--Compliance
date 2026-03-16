package com.aiaudit.platform.gpai;

import com.aiaudit.platform.auth.AppUser;
import com.aiaudit.platform.common.ApiResponse;
import com.aiaudit.platform.compliance.ObligationStatus;
import com.aiaudit.platform.gpai.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/gpai-models")
@RequiredArgsConstructor
public class GpaiController {

    private final GpaiService gpaiService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<GpaiModelDto>>> listModels(@AuthenticationPrincipal AppUser user) {
        return ResponseEntity.ok(ApiResponse.success(gpaiService.listModels(user.getOrganization().getId())));
    }

    @GetMapping("/{modelId}")
    public ResponseEntity<ApiResponse<GpaiModelDto>> getModel(
            @AuthenticationPrincipal AppUser user, @PathVariable UUID modelId) {
        return ResponseEntity.ok(ApiResponse.success(gpaiService.getModel(modelId, user.getOrganization().getId())));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<GpaiModelDto>> createModel(
            @AuthenticationPrincipal AppUser user,
            @Valid @RequestBody CreateGpaiModelRequest request) {
        GpaiModelDto dto = gpaiService.createModel(user.getOrganization().getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(dto, "GPAI model created"));
    }

    @DeleteMapping("/{modelId}")
    public ResponseEntity<ApiResponse<Void>> deleteModel(
            @AuthenticationPrincipal AppUser user, @PathVariable UUID modelId) {
        gpaiService.deleteModel(modelId, user.getOrganization().getId());
        return ResponseEntity.ok(ApiResponse.success(null, "GPAI model deleted"));
    }

    @GetMapping("/{modelId}/obligations")
    public ResponseEntity<ApiResponse<List<GpaiObligationDto>>> getObligations(@PathVariable UUID modelId) {
        return ResponseEntity.ok(ApiResponse.success(gpaiService.getObligations(modelId)));
    }

    @PatchMapping("/{modelId}/obligations/{obligationId}")
    public ResponseEntity<ApiResponse<GpaiObligationDto>> updateObligation(
            @PathVariable UUID obligationId,
            @RequestBody Map<String, String> body) {
        ObligationStatus status = ObligationStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(ApiResponse.success(gpaiService.updateObligationStatus(obligationId, status)));
    }
}
