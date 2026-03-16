package com.aiaudit.platform.document;

import com.aiaudit.platform.auth.AppUser;
import com.aiaudit.platform.common.ApiResponse;
import com.aiaudit.platform.common.exception.BadRequestException;
import com.aiaudit.platform.document.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/ai-systems/{aiSystemId}/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentGenerationService documentService;
    private final DocumentExportService exportService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<DocumentDto>>> getDocuments(
            @AuthenticationPrincipal AppUser user,
            @PathVariable UUID aiSystemId) {
        List<DocumentDto> docs = documentService.getDocuments(aiSystemId, user.getOrganization().getId());
        return ResponseEntity.ok(ApiResponse.success(docs));
    }

    @GetMapping("/{documentId}")
    public ResponseEntity<ApiResponse<DocumentDto>> getDocument(@PathVariable UUID documentId) {
        return ResponseEntity.ok(ApiResponse.success(documentService.getDocument(documentId)));
    }

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<DocumentDto>> generateDocument(
            @AuthenticationPrincipal AppUser user,
            @PathVariable UUID aiSystemId,
            @Valid @RequestBody GenerateDocumentRequest request) {
        DocumentDto dto = documentService.startGeneration(aiSystemId, user.getOrganization().getId(), request, user);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ApiResponse.success(dto, "Document generation started"));
    }

    @PutMapping("/{documentId}")
    public ResponseEntity<ApiResponse<DocumentDto>> updateDocument(
            @PathVariable UUID documentId,
            @Valid @RequestBody UpdateDocumentRequest request) {
        return ResponseEntity.ok(ApiResponse.success(documentService.updateDocument(documentId, request)));
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(@PathVariable UUID documentId) {
        documentService.deleteDocument(documentId);
        return ResponseEntity.ok(ApiResponse.success(null, "Document deleted"));
    }

    @PostMapping("/{documentId}/regenerate")
    public ResponseEntity<ApiResponse<DocumentDto>> regenerateDocument(
            @AuthenticationPrincipal AppUser user,
            @PathVariable UUID documentId) {
        DocumentDto dto = documentService.regenerateDocument(documentId, user);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ApiResponse.success(dto, "Regeneration started"));
    }

    @GetMapping("/{documentId}/export/pdf")
    public ResponseEntity<byte[]> exportPdf(@PathVariable UUID documentId) {
        DocumentDto doc = documentService.getDocument(documentId);
        if (doc.getContent() == null || doc.getContent().isBlank()) {
            throw new BadRequestException("Document has no content to export");
        }

        byte[] pdf = exportService.exportToPdf(doc.getContent(), doc.getTitle());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(sanitizeFilename(doc.getTitle()) + ".pdf").build());

        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }

    @GetMapping("/{documentId}/export/docx")
    public ResponseEntity<byte[]> exportDocx(@PathVariable UUID documentId) {
        DocumentDto doc = documentService.getDocument(documentId);
        if (doc.getContent() == null || doc.getContent().isBlank()) {
            throw new BadRequestException("Document has no content to export");
        }

        byte[] docx = exportService.exportToDocx(doc.getContent(), doc.getTitle());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(sanitizeFilename(doc.getTitle()) + ".docx").build());

        return new ResponseEntity<>(docx, headers, HttpStatus.OK);
    }

    private String sanitizeFilename(String name) {
        return name.replaceAll("[^a-zA-Z0-9\\-_ ]", "").replaceAll("\\s+", "_");
    }
}
