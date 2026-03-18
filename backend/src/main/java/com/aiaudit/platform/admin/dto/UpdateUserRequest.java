package com.aiaudit.platform.admin.dto;

import com.aiaudit.platform.auth.UserRole;
import lombok.Data;

@Data
public class UpdateUserRequest {
    private UserRole role;
    private Boolean enabled;
}
