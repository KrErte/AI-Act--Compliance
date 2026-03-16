package com.aiaudit.platform.team.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AcceptInvitationRequest {
    @NotBlank
    private String token;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank @Size(min = 8)
    private String password;
}
