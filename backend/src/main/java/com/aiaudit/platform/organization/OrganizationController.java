package com.aiaudit.platform.organization;

import com.aiaudit.platform.auth.AppUser;
import com.aiaudit.platform.common.ApiResponse;
import com.aiaudit.platform.organization.dto.OrganizationDto;
import com.aiaudit.platform.organization.dto.UpdateOrganizationRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<OrganizationDto>> getMyOrganization(@AuthenticationPrincipal AppUser user) {
        OrganizationDto dto = organizationService.getOrganization(user.getOrganization().getId());
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<OrganizationDto>> updateMyOrganization(
            @AuthenticationPrincipal AppUser user,
            @Valid @RequestBody UpdateOrganizationRequest request) {
        OrganizationDto dto = organizationService.updateOrganization(user.getOrganization().getId(), request);
        return ResponseEntity.ok(ApiResponse.success(dto, "Organization updated"));
    }
}
