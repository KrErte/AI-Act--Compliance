package com.aiaudit.platform.aisystem;

import com.aiaudit.platform.aisystem.dto.*;
import com.aiaudit.platform.auth.AppUser;
import com.aiaudit.platform.common.ApiResponse;
import com.aiaudit.platform.common.PagedResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/ai-systems")
@RequiredArgsConstructor
public class AiSystemController {

    private final AiSystemService aiSystemService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<AiSystemDto>>> listAiSystems(
            @AuthenticationPrincipal AppUser user,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) RiskLevel riskLevel,
            @RequestParam(required = false) AiSystemStatus status,
            @RequestParam(required = false) ComplianceStatus complianceStatus,
            @RequestParam(required = false) DeploymentContext deploymentContext,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PagedResponse<AiSystemDto> result = aiSystemService.listAiSystems(
                user.getOrganization().getId(), search, riskLevel, status,
                complianceStatus, deploymentContext, page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AiSystemDto>> getAiSystem(
            @AuthenticationPrincipal AppUser user,
            @PathVariable UUID id) {
        AiSystemDto dto = aiSystemService.getAiSystem(id, user.getOrganization().getId());
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AiSystemDto>> createAiSystem(
            @AuthenticationPrincipal AppUser user,
            @Valid @RequestBody CreateAiSystemRequest request) {
        AiSystemDto dto = aiSystemService.createAiSystem(user.getOrganization().getId(), request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(dto, "AI system created"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AiSystemDto>> updateAiSystem(
            @AuthenticationPrincipal AppUser user,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAiSystemRequest request) {
        AiSystemDto dto = aiSystemService.updateAiSystem(id, user.getOrganization().getId(), request, user);
        return ResponseEntity.ok(ApiResponse.success(dto, "AI system updated"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAiSystem(
            @AuthenticationPrincipal AppUser user,
            @PathVariable UUID id) {
        aiSystemService.deleteAiSystem(id, user.getOrganization().getId(), user);
        return ResponseEntity.ok(ApiResponse.success(null, "AI system deleted"));
    }
}
