package com.aiaudit.platform.alert;

import com.aiaudit.platform.alert.dto.AlertDto;
import com.aiaudit.platform.auth.AppUser;
import com.aiaudit.platform.common.ApiResponse;
import com.aiaudit.platform.common.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<AlertDto>>> getAlerts(
            @AuthenticationPrincipal AppUser user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                alertService.getAlerts(user.getOrganization().getId(), page, size)));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount(@AuthenticationPrincipal AppUser user) {
        long count = alertService.getUnreadCount(user.getOrganization().getId());
        return ResponseEntity.ok(ApiResponse.success(Map.of("count", count)));
    }

    @PatchMapping("/{alertId}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable UUID alertId) {
        alertService.markAsRead(alertId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/mark-all-read")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(@AuthenticationPrincipal AppUser user) {
        alertService.markAllAsRead(user.getOrganization().getId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PatchMapping("/{alertId}/dismiss")
    public ResponseEntity<ApiResponse<Void>> dismissAlert(@PathVariable UUID alertId) {
        alertService.dismissAlert(alertId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
