package com.aiaudit.platform.team.dto;

import com.aiaudit.platform.auth.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InviteRequest {
    @NotBlank @Email
    private String email;

    @NotNull
    private UserRole role;
}
