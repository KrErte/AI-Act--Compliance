package com.aiaudit.platform.audit;

import com.aiaudit.platform.audit.dto.AuditLogDto;
import com.aiaudit.platform.auth.AppUser;
import com.aiaudit.platform.common.ApiResponse;
import com.aiaudit.platform.common.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping("/audit-log")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLogDto>>> getAuditLog(
            @AuthenticationPrincipal AppUser user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PagedResponse<AuditLogDto> result = auditService.getAuditLog(
                user.getOrganization().getId(), page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/ai-systems/{aiSystemId}/audit-log")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLogDto>>> getAiSystemAuditLog(
            @PathVariable UUID aiSystemId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PagedResponse<AuditLogDto> result = auditService.getEntityAuditLog(
                "AI_SYSTEM", aiSystemId, page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
