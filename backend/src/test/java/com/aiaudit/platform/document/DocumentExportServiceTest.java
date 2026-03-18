package com.aiaudit.platform.document;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DocumentExportServiceTest {

    private DocumentExportService service;

    @BeforeEach
    void setUp() {
        service = new DocumentExportService();
    }

    // ── exportToPdf ──

    @Nested
    @DisplayName("exportToPdf")
    class ExportToPdfTests {

        @Test
        @DisplayName("returns non-null byte array for valid markdown content")
        void returnsNonNullBytes() {
            byte[] result = service.exportToPdf("# Title\nContent", "Test Title");

            assertNotNull(result);
            assertTrue(result.length > 0, "PDF byte array should not be empty");
        }

        @Test
        @DisplayName("does not throw for empty content")
        void handlesEmptyContent() {
            assertDoesNotThrow(() -> {
                byte[] result = service.exportToPdf("", "Test Title");
                assertNotNull(result);
            });
        }
    }

    // ── exportToDocx ──

    @Nested
    @DisplayName("exportToDocx")
    class ExportToDocxTests {

        @Test
        @DisplayName("returns non-null byte array for markdown with headings and lists")
        void returnsNonNullBytes() {
            String markdown = "# Title\n## Subtitle\n- Item\nParagraph";

            byte[] result = service.exportToDocx(markdown, "Test Title");

            assertNotNull(result);
            assertTrue(result.length > 0, "DOCX byte array should not be empty");
        }

        @Test
        @DisplayName("handles markdown formatting (bold, italic, code)")
        void handlesMarkdownFormatting() {
            String markdown = "**bold text** and *italic text* and `inline code`";

            byte[] result = service.exportToDocx(markdown, "Test Title");

            assertNotNull(result);
            assertTrue(result.length > 0, "DOCX byte array should not be empty");
        }
    }
}
