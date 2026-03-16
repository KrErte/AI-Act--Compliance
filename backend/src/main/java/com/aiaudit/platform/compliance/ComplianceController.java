package com.aiaudit.platform.compliance;

import com.aiaudit.platform.auth.AppUser;
import com.aiaudit.platform.common.ApiResponse;
import com.aiaudit.platform.compliance.dto.ObligationDto;
import com.aiaudit.platform.compliance.dto.UpdateObligationRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/ai-systems/{aiSystemId}/obligations")
@RequiredArgsConstructor
public class ComplianceController {

    private final ComplianceObligationService obligationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ObligationDto>>> getObligations(
            @AuthenticationPrincipal AppUser user,
            @PathVariable UUID aiSystemId) {
        List<ObligationDto> obligations = obligationService.getObligations(
                aiSystemId, user.getOrganization().getId());
        return ResponseEntity.ok(ApiResponse.success(obligations));
    }

    @PatchMapping("/{obligationId}")
    public ResponseEntity<ApiResponse<ObligationDto>> updateObligation(
            @AuthenticationPrincipal AppUser user,
            @PathVariable UUID aiSystemId,
            @PathVariable UUID obligationId,
            @Valid @RequestBody UpdateObligationRequest request) {
        ObligationDto dto = obligationService.updateObligation(
                obligationId, aiSystemId, user.getOrganization().getId(), request, user);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }
}
