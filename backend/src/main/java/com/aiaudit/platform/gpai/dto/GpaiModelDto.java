package com.aiaudit.platform.gpai.dto;

import com.aiaudit.platform.gpai.GpaiModel;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class GpaiModelDto {
    private UUID id;
    private String name;
    private String provider;
    private String modelType;
    private boolean hasSystemicRisk;
    private BigDecimal trainingComputeFlops;
    private String description;
    private String version;
    private boolean openSource;
    private Instant createdAt;

    public static GpaiModelDto from(GpaiModel model) {
        return GpaiModelDto.builder()
                .id(model.getId())
                .name(model.getName())
                .provider(model.getProvider())
                .modelType(model.getModelType() != null ? model.getModelType().name() : null)
                .hasSystemicRisk(model.isHasSystemicRisk())
                .trainingComputeFlops(model.getTrainingComputeFlops())
                .description(model.getDescription())
                .version(model.getVersion())
                .openSource(model.isOpenSource())
                .createdAt(model.getCreatedAt())
                .build();
    }
}
