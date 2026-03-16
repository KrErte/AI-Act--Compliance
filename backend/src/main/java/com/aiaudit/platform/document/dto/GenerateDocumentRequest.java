package com.aiaudit.platform.document.dto;

import com.aiaudit.platform.document.DocumentType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GenerateDocumentRequest {
    @NotNull
    private DocumentType documentType;

    private String additionalContext;
}
