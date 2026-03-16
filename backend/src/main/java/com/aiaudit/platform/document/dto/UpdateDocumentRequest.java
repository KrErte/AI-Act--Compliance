package com.aiaudit.platform.document.dto;

import lombok.Data;

@Data
public class UpdateDocumentRequest {
    private String title;
    private String content;
}
