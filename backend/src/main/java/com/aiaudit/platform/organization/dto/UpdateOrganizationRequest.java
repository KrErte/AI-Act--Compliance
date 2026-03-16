package com.aiaudit.platform.organization.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateOrganizationRequest {

    @NotBlank(message = "Organization name is required")
    private String name;

    private String industry;
    private String country;
}
