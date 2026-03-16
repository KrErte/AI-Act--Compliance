package com.aiaudit.platform.compliance;

import com.aiaudit.platform.auth.AppUser;
import com.aiaudit.platform.common.ApiResponse;
import com.aiaudit.platform.compliance.dto.TaskObligationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/compliance")
@RequiredArgsConstructor
public class MyTasksController {

    private final ComplianceObligationRepository obligationRepository;

    @GetMapping("/my-tasks")
    public ResponseEntity<ApiResponse<List<TaskObligationDto>>> getMyTasks(
            @AuthenticationPrincipal AppUser user) {
        List<TaskObligationDto> tasks = obligationRepository
                .findByAssignedToIdOrderByDueDateAsc(user.getId())
                .stream()
                .map(TaskObligationDto::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(tasks));
    }
}
