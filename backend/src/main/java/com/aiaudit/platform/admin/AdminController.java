package com.aiaudit.platform.admin;

import com.aiaudit.platform.admin.dto.AdminStatsDto;
import com.aiaudit.platform.admin.dto.AdminUserDto;
import com.aiaudit.platform.admin.dto.UpdateUserRequest;
import com.aiaudit.platform.audit.dto.AuditLogDto;
import com.aiaudit.platform.auth.AppUser;
import com.aiaudit.platform.common.ApiResponse;
import com.aiaudit.platform.common.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<PagedResponse<AdminUserDto>>> getUsers(
            @AuthenticationPrincipal AppUser user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var result = adminService.getUsers(user.getOrganization().getId(), page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<ApiResponse<AdminUserDto>> updateUser(
            @PathVariable UUID id,
            @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal AppUser admin) {
        var result = adminService.updateUser(id, request, admin);
        return ResponseEntity.ok(ApiResponse.success(result, "User updated successfully"));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(
            @PathVariable UUID id,
            @AuthenticationPrincipal AppUser admin) {
        adminService.deactivateUser(id, admin);
        return ResponseEntity.ok(ApiResponse.success(null, "User deactivated"));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<AdminStatsDto>> getStats(
            @AuthenticationPrincipal AppUser user) {
        var result = adminService.getStats(user.getOrganization().getId());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/audit-log")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLogDto>>> getAuditLog(
            @AuthenticationPrincipal AppUser user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var result = adminService.getAuditLog(user.getOrganization().getId(), page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
