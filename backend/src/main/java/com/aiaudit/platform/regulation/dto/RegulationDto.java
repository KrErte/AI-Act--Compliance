package com.aiaudit.platform.regulation.dto;

import com.aiaudit.platform.regulation.Regulation;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class RegulationDto {
    private UUID id;
    private String code;
    private String name;
    private String description;
    private LocalDate effectiveDate;
    private int domainCount;
    private int questionCount;

    public static RegulationDto from(Regulation r, int questionCount) {
        return RegulationDto.builder()
                .id(r.getId())
                .code(r.getCode())
                .name(r.getName())
                .description(r.getDescription())
                .effectiveDate(r.getEffectiveDate())
                .domainCount(r.getDomains().size())
                .questionCount(questionCount)
                .build();
    }
}
