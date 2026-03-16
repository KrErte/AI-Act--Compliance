package com.aiaudit.platform.gpai.dto;

import com.aiaudit.platform.gpai.GpaiModelType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateGpaiModelRequest {
    @NotBlank
    private String name;
    private String provider;
    private GpaiModelType modelType;
    private boolean hasSystemicRisk;
    private BigDecimal trainingComputeFlops;
    private String description;
    private String version;
    private boolean openSource;
}
