package com.aiaudit.platform.document.dto;

import com.aiaudit.platform.document.GeneratedDocument;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class DocumentDto {
    private UUID id;
    private UUID aiSystemId;
    private String documentType;
    private String title;
    private String content;
    private String status;
    private int version;
    private String generatedBy;
    private Instant createdAt;
    private Instant updatedAt;

    public static DocumentDto from(GeneratedDocument doc) {
        String generatedBy = null;
        if (doc.getGeneratedBy() != null) {
            generatedBy = doc.getGeneratedBy().getFirstName() + " " + doc.getGeneratedBy().getLastName();
        }
        return DocumentDto.builder()
                .id(doc.getId())
                .aiSystemId(doc.getAiSystem().getId())
                .documentType(doc.getDocumentType().name())
                .title(doc.getTitle())
                .content(doc.getContent())
                .status(doc.getStatus().name())
                .version(doc.getVersion())
                .generatedBy(generatedBy)
                .createdAt(doc.getCreatedAt())
                .updatedAt(doc.getUpdatedAt())
                .build();
    }
}
