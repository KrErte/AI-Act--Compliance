package com.aiaudit.platform.document;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
@Slf4j
public class DocumentExportService {

    private final Parser markdownParser;
    private final HtmlRenderer htmlRenderer;

    public DocumentExportService() {
        MutableDataSet options = new MutableDataSet();
        this.markdownParser = Parser.builder(options).build();
        this.htmlRenderer = HtmlRenderer.builder(options).build();
    }

    public byte[] exportToPdf(String markdownContent, String title) {
        try {
            String html = markdownToHtml(markdownContent, title);

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(os);
            builder.run();

            return os.toByteArray();
        } catch (Exception e) {
            log.error("PDF export failed", e);
            throw new RuntimeException("Failed to export document as PDF: " + e.getMessage(), e);
        }
    }

    public byte[] exportToDocx(String markdownContent, String title) {
        try (XWPFDocument document = new XWPFDocument();
             ByteArrayOutputStream os = new ByteArrayOutputStream()) {

            // Title
            XWPFParagraph titlePara = document.createParagraph();
            titlePara.setAlignment(ParagraphAlignment.CENTER);
            titlePara.setSpacingAfter(400);
            XWPFRun titleRun = titlePara.createRun();
            titleRun.setText(title);
            titleRun.setBold(true);
            titleRun.setFontSize(18);
            titleRun.setFontFamily("Calibri");

            // Parse markdown line by line
            String[] lines = markdownContent.split("\n");
            boolean inList = false;

            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) {
                    document.createParagraph();
                    inList = false;
                    continue;
                }

                XWPFParagraph para = document.createParagraph();

                if (trimmed.startsWith("# ")) {
                    addHeading(para, trimmed.substring(2), 1);
                } else if (trimmed.startsWith("## ")) {
                    addHeading(para, trimmed.substring(3), 2);
                } else if (trimmed.startsWith("### ")) {
                    addHeading(para, trimmed.substring(4), 3);
                } else if (trimmed.startsWith("- ") || trimmed.startsWith("* ")) {
                    addListItem(para, trimmed.substring(2));
                    inList = true;
                } else if (trimmed.matches("^\\d+\\.\\s.*")) {
                    addListItem(para, trimmed.replaceFirst("^\\d+\\.\\s", ""));
                    inList = true;
                } else if (trimmed.startsWith("---") || trimmed.startsWith("***")) {
                    // Horizontal rule - skip
                } else {
                    addParagraph(para, trimmed);
                }
            }

            document.write(os);
            return os.toByteArray();
        } catch (IOException e) {
            log.error("DOCX export failed", e);
            throw new RuntimeException("Failed to export document as DOCX: " + e.getMessage(), e);
        }
    }

    private String markdownToHtml(String markdown, String title) {
        Node document = markdownParser.parse(markdown);
        String body = htmlRenderer.render(document);

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8"/>
                <style>
                    body {
                        font-family: 'Helvetica', 'Arial', sans-serif;
                        font-size: 11pt;
                        line-height: 1.6;
                        color: #1a1a1a;
                        margin: 2cm;
                    }
                    h1 {
                        font-size: 20pt;
                        color: #1e40af;
                        border-bottom: 2px solid #1e40af;
                        padding-bottom: 8px;
                        margin-top: 24px;
                    }
                    h2 {
                        font-size: 16pt;
                        color: #1e3a5f;
                        margin-top: 20px;
                        border-bottom: 1px solid #e2e8f0;
                        padding-bottom: 6px;
                    }
                    h3 {
                        font-size: 13pt;
                        color: #334155;
                        margin-top: 16px;
                    }
                    ul, ol { margin-left: 20px; }
                    li { margin-bottom: 4px; }
                    p { margin-bottom: 8px; }
                    table {
                        width: 100%%;
                        border-collapse: collapse;
                        margin: 12px 0;
                    }
                    th, td {
                        border: 1px solid #d1d5db;
                        padding: 8px 12px;
                        text-align: left;
                        font-size: 10pt;
                    }
                    th {
                        background: #f1f5f9;
                        font-weight: bold;
                    }
                    .header {
                        text-align: center;
                        margin-bottom: 30px;
                        padding-bottom: 20px;
                        border-bottom: 3px solid #1e40af;
                    }
                    .header h1 {
                        border: none;
                        margin: 0;
                        padding: 0;
                    }
                    .header .subtitle {
                        color: #64748b;
                        font-size: 10pt;
                        margin-top: 8px;
                    }
                    .footer {
                        text-align: center;
                        font-size: 8pt;
                        color: #94a3b8;
                        margin-top: 40px;
                        padding-top: 12px;
                        border-top: 1px solid #e2e8f0;
                    }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>%s</h1>
                    <div class="subtitle">Generated by AIAudit — EU AI Act Compliance Platform</div>
                </div>
                %s
                <div class="footer">
                    This document was generated by AIAudit (aiaudit.eu) for EU AI Act compliance purposes.
                    It should be reviewed by qualified compliance personnel before use.
                </div>
            </body>
            </html>
            """.formatted(title, body);
    }

    private void addHeading(XWPFParagraph para, String text, int level) {
        para.setSpacingBefore(200);
        para.setSpacingAfter(100);
        XWPFRun run = para.createRun();
        run.setText(cleanMarkdown(text));
        run.setBold(true);
        run.setFontFamily("Calibri");
        switch (level) {
            case 1 -> { run.setFontSize(16); run.setColor("1e40af"); }
            case 2 -> { run.setFontSize(14); run.setColor("1e3a5f"); }
            case 3 -> { run.setFontSize(12); run.setColor("334155"); }
        }
    }

    private void addListItem(XWPFParagraph para, String text) {
        para.setIndentationLeft(720); // 0.5 inch
        XWPFRun bullet = para.createRun();
        bullet.setText("\u2022  ");
        bullet.setFontSize(10);
        XWPFRun run = para.createRun();
        run.setText(cleanMarkdown(text));
        run.setFontSize(10);
        run.setFontFamily("Calibri");
    }

    private void addParagraph(XWPFParagraph para, String text) {
        XWPFRun run = para.createRun();
        run.setText(cleanMarkdown(text));
        run.setFontSize(10);
        run.setFontFamily("Calibri");
    }

    private String cleanMarkdown(String text) {
        return text
                .replaceAll("\\*\\*(.+?)\\*\\*", "$1")
                .replaceAll("\\*(.+?)\\*", "$1")
                .replaceAll("`(.+?)`", "$1")
                .replaceAll("\\[(.+?)\\]\\(.+?\\)", "$1");
    }
}
