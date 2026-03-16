package com.aiaudit.platform.gpai.dto;

import com.aiaudit.platform.gpai.GpaiObligation;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class GpaiObligationDto {
    private UUID id;
    private String articleRef;
    private String title;
    private String description;
    private String status;

    public static GpaiObligationDto from(GpaiObligation o) {
        return GpaiObligationDto.builder()
                .id(o.getId())
                .articleRef(o.getArticleRef())
                .title(o.getTitle())
                .description(o.getDescription())
                .status(o.getStatus().name())
                .build();
    }
}
