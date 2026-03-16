package com.aiaudit.platform.subscription;

import com.aiaudit.platform.auth.AppUser;
import com.aiaudit.platform.common.ApiResponse;
import com.aiaudit.platform.subscription.dto.SubscriptionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping
    public ResponseEntity<ApiResponse<SubscriptionDto>> getSubscription(@AuthenticationPrincipal AppUser user) {
        SubscriptionDto dto = subscriptionService.getSubscription(user.getOrganization().getId());
        return ResponseEntity.ok(ApiResponse.success(dto));
    }
}
