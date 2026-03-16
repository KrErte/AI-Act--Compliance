package com.aiaudit.platform.aisystem.dto;

import com.aiaudit.platform.aisystem.DeploymentContext;
import com.aiaudit.platform.aisystem.OrganizationRole;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateAiSystemRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;
    private String vendor;
    private String version;
    private String purpose;
    private DeploymentContext deploymentContext;
    private OrganizationRole organizationRole;
}
