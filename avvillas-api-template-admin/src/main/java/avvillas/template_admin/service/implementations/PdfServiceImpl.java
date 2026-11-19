package avvillas.template_admin.service.implementations;

import avvillas.template_admin.constant.MessageConstant;
import avvillas.template_admin.constant.message.FormatMessage;
import avvillas.template_admin.constant.message.PdfMessage;
import avvillas.template_admin.constant.PaperType;
import avvillas.template_admin.service.FormatService;
import avvillas.template_admin.service.PdfService;
import avvillas.template_admin.service.dto.format.FormatDto;
import avvillas.template_admin.service.dto.format.FormatPageDto;
import avvillas.template_admin.service.dto.format.FormatPageResponseDto;
import avvillas.template_admin.service.dto.object.TableHeaderConfig;
import avvillas.template_admin.service.dto.object.response.CellResponseDTO;
import avvillas.template_admin.service.dto.object.response.ObjectGenericResponseDTO;
import avvillas.template_admin.service.dto.object.response.ObjectImageResponseDTO;
import avvillas.template_admin.service.dto.object.response.ObjectResponseDTO;
import avvillas.template_admin.service.dto.object.response.ObjectTableResponseDTO;
import avvillas.template_admin.util.ImageUtil;
import avvillas.template_admin.util.PdfUtil;
import avvillas.template_admin.web.controller.exception.PdfProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PdfServiceImpl implements PdfService {

    private final FormatService formatService;
    private static final String DIV = " </div>";
    private static final String BR = "<br/>";
    private static final String NEW_LINE = "\n";
    private static final int FIRST_POSITION = 0;

    private static final String LINE_BREAK_STYLE = "word-break:normal; overflow-wrap:normal;";
    private static final String MULTILINE_STYLE = "white-space:nowrap;" + LINE_BREAK_STYLE;
    private static final String SINGLE_LINE_STYLE = "white-space:nowrap;" + LINE_BREAK_STYLE;

    private static final String TYPE_TABLE = "TABLE";
    private static final String TYPE_TEXT = "TEXT";
    private static final String TYPE_SHAPE = "SHAPE";
    private static final String TYPE_IMAGE = "IMAGE";
    private static final String ERROR_IMAGE = "Error al procesar la imagen";

    private static final String FONT_SIZE_PATTERN = "font-size\\s*:\\s*(\\d+)px";
    private static final String TOP_PATTERN = "top\\s*:\\s*(\\d+)px";

    @Autowired
    public PdfServiceImpl(@Lazy FormatService formatService) {
        this.formatService = formatService;
    }


    public byte[] generatePdf(String formatId) {
        Page<FormatPageResponseDto> formatPageResponse = formatService.searchGlobal(formatId, Pageable.unpaged());

        if (formatPageResponse.getContent().isEmpty())
            throw new NoSuchElementException(MessageConstant.format(FormatMessage.FORMAT_NOT_FOUND, formatId));

        FormatPageResponseDto dto = formatPageResponse.getContent().get(FIRST_POSITION);
        String htmlContent = generateHtmlFromDto(dto.getPdfConfig(), dto.getPages());

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(htmlContent);
            renderer.layout();
            renderer.createPDF(outputStream);
            byte[] pdfBytes = outputStream.toByteArray();
            String base64Content = java.util.Base64.getEncoder().encodeToString(pdfBytes);
            formatService.updateFormatContent(formatId, htmlContent, base64Content);
            return pdfBytes;
        } catch (Exception e) {
            throw new PdfProcessingException(MessageConstant.format(PdfMessage.ERROR_PDF, e), e);
        }
    }

    @Override
    public String generateHtmlFromDto(FormatDto.PdfConfigDto pdfConfig, List<FormatPageDto> pages) {
        FormatDto.MarginDto margins = pdfConfig.getMargins();

        String pageSize = switch (pdfConfig.getPaperType()) {
            case PaperType.CARTA -> PaperType.LETTER;
            case PaperType.OFICIO -> PaperType.LEGAL;
            default -> PaperType.A4;
        };

        String styles = String.format(
                Locale.US,
                "@page { size: %s; margin-top: 0; margin-right: 0; margin-bottom: %.2fpt; margin-left: 0 } " +
                        "body { font-family: %s; margin: 0; }",
                pageSize,
                cmToPt(margins.getBottom().doubleValue()),
                pdfConfig.getFontFamily()
        );

        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>");
        html.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
        html.append("<head>");
        html.append("<meta charset=\"UTF-8\" />");
        html.append("<style>");
        html.append(styles);
        html.append(".page { border: 1px solid transparent; page-break-after: always; break-after: page; position: relative; }");
        html.append(".object { position: absolute; }");
        html.append("table { border-collapse: collapse; width: 100%; }");
        html.append("th, td { border: 1px solid black; padding: 5px; text-align: left; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");

        pages.stream()
                .sorted(Comparator.comparingInt(FormatPageDto::getPageNumber))
                .forEach(page -> {
                    html.append("<div class='page'>");
                    html.append("<div style='height: 1px; visibility: hidden;'>placeholder</div>");

                    for (ObjectResponseDTO object : page.getObjects()) {
                        String objectHtml = getObjectHtml(object);
                        html.append(objectHtml);
                    }

                    html.append(DIV);
                });

        html.append("</body>");
        html.append("</html>");

        return html.toString();

    }

    private String getObjectHtml(ObjectResponseDTO object) {
        StringBuilder htmlBuilder = new StringBuilder();
        String baseStyle = buildBaseStyle(object);

        buildHtmlForObject(htmlBuilder, baseStyle, object);

        return htmlBuilder.toString();
    }

    private void buildHtmlForObject(StringBuilder htmlBuilder, String baseStyle, ObjectResponseDTO object) {
        Map<String, Consumer<ObjectResponseDTO>> handlers = createHtmlHandlers(htmlBuilder, baseStyle);

        Consumer<ObjectResponseDTO> handler = handlers.getOrDefault(object.getType(), obj -> appendDefaultHtml(htmlBuilder, baseStyle, obj));
        handler.accept(object);
    }

    private Map<String, Consumer<ObjectResponseDTO>> createHtmlHandlers(StringBuilder htmlBuilder, String baseStyle) {
        Map<String, Consumer<ObjectResponseDTO>> handlers = new HashMap<>();
        handlers.put(TYPE_TABLE, obj -> appendTableHtml(htmlBuilder, baseStyle, obj));
        handlers.put(TYPE_TEXT, obj -> appendTextHtml(htmlBuilder, baseStyle, obj));
        handlers.put(TYPE_SHAPE, obj -> appendShapeHtml(htmlBuilder, baseStyle, obj));
        handlers.put(TYPE_IMAGE, obj -> appendImageHtml(htmlBuilder, baseStyle, obj));
        return handlers;
    }

    private String buildBaseStyle(ObjectResponseDTO object) {
        String style = "top: " + object.getPositionY() + "px; left: " + (object.getPositionX()) + "px;";
        if (object.getStyle() != null && !object.getStyle().isEmpty()) style += PdfUtil.parseStyles(object.getStyle());
        return style;
    }

    private void appendTableHtml(StringBuilder htmlBuilder, String style, ObjectResponseDTO object) {
        htmlBuilder.append(buildObjectDivOpenTag(object, style))
                .append(buildTableHtml((ObjectTableResponseDTO) object))
                .append(DIV);
    }

    private void appendTextHtml(StringBuilder htmlBuilder, String style, ObjectResponseDTO object) {
        if (!(object instanceof ObjectGenericResponseDTO genericResponse)) return;

        String content = genericResponse.getContent();

        if (content.contains("•")) appendBulletList(htmlBuilder, style, content, object);
        else appendTextBlock(htmlBuilder, style, content, object);

    }

    private void appendBulletList(StringBuilder htmlBuilder, String style, String content, ObjectResponseDTO object) {
        htmlBuilder.append(buildObjectDivOpenTag(object, "z-index: 10000; " + style))
            .append("<ul>");

        for (String line : content.split(NEW_LINE)) {
            if (line.startsWith("•")) {
                htmlBuilder.append("<li>")
                        .append(line.substring(1).trim())
                        .append("</li>");
            }
        }

        htmlBuilder.append("</ul></div>");
    }

    private void appendTextBlock(StringBuilder htmlBuilder, String style, String content, ObjectResponseDTO object) {
        boolean isMultiLine = content.contains(NEW_LINE);

        String adjustedStyle = isMultiLine
                ? style
                : adjustTopPosition(style, getFontSizeDelta(style));

        String formattedContent = isMultiLine
                ? formatMultiLineContent(content)
                : formatSingleLineContent(content);

        String textStyle = isMultiLine ? MULTILINE_STYLE : SINGLE_LINE_STYLE;

        String composedStyle = "position:absolute;"
            + "z-index:1000;"
            + "display:inline-block;"
            + "line-height:1.5;"
            + textStyle
            + adjustedStyle;

        htmlBuilder.append(buildObjectDivOpenTag(object, composedStyle))
                .append(formattedContent)
                .append(DIV);
    }

    private String formatMultiLineContent(String content) {
        String[] lines = content.split(NEW_LINE, -1);
        for (int i = FIRST_POSITION; i < lines.length; i++)
            lines[i] = lines[i].replace(" ", "\u00A0");

        return String.join(BR, lines);
    }

    private String formatSingleLineContent(String content) {
        return content.replace(" ", "\u00A0");
    }

    private int getFontSizeDelta(String style) {
        final Matcher fontSizeMatcher = Pattern.compile(FONT_SIZE_PATTERN, Pattern.CASE_INSENSITIVE).matcher(style);
        if (fontSizeMatcher.find()) {
            int fontSize = Integer.parseInt(fontSizeMatcher.group(1));
            if (fontSize == 8) return 9;
            if (fontSize == 9) return 8;
            if (fontSize == 10) return 9;
            if (fontSize == 11) return 5;
        }
        return 6;
    }

    private String adjustTopPosition(String style, int delta) {
        final Matcher topMatcher = Pattern.compile(TOP_PATTERN).matcher(style);
        if (topMatcher.find()) {
            int currentTop = Integer.parseInt(topMatcher.group(1));
            int updatedTop = currentTop + delta;
            return topMatcher.replaceFirst("top: " + updatedTop + "px");
        }
        return style;
    }

    private void appendShapeHtml(StringBuilder htmlBuilder, String style, ObjectResponseDTO object) {
        htmlBuilder.append(buildObjectDivOpenTag(object, style + "; border: 1px solid transparent;"))
                .append(" </div>");
    }

    private void appendImageHtml(StringBuilder htmlBuilder, String style, ObjectResponseDTO object) {
        if (object instanceof ObjectImageResponseDTO imageResponse) {
            String imageStyle = object.getStyle() != null ? PdfUtil.parseStyles(object.getStyle()) : "";
            htmlBuilder.append(buildObjectDivOpenTag(object, style))
                    .append(ImageUtil.buildImageHtml(imageResponse, imageStyle))
                    .append(DIV);
        } else {
            htmlBuilder.append("<div class='object'>")
                    .append(ERROR_IMAGE)
                    .append(DIV);
        }
    }

    private String buildTableHtml(ObjectTableResponseDTO tableObject) {
        StringBuilder tableHtml = new StringBuilder();

        tableHtml.append("<table>");
        tableHtml.append("<thead><tr>");
        for (TableHeaderConfig.ColumnConfig column : tableObject.getHeader().getColumns()) {
            tableHtml.append("<th style='text-align: ")
                    .append(column.getStyle().getOrDefault("textAlign", "left"))
                    .append(";'>")
                    .append(column.getLabel())
                    .append("</th>");
        }
        tableHtml.append("</tr></thead>");

        tableHtml.append("<tbody>");
        for (CellResponseDTO cell : tableObject.getBody().getCells()) {
            if (cell.getColumnIndex() == FIRST_POSITION) tableHtml.append("<tr>");

            tableHtml.append("<td style='");
            if (cell.getStyle() != null) {
                for (Map.Entry<String, Object> style : cell.getStyle().entrySet()) {
                    tableHtml.append(style.getKey()).append(":").append(style.getValue()).append(";");
                }
            }
            tableHtml.append("'>");
            tableHtml.append(cell.getContent() != null ? cell.getContent() : "");
            tableHtml.append("</td>");

            if (cell.getColumnIndex() == tableObject.getHeader().getColumns().size() - 1) {
                tableHtml.append("</tr>");
            }
        }
        tableHtml.append("</tbody>");

        tableHtml.append("</table>");
        return tableHtml.toString();
    }

    private void appendDefaultHtml(StringBuilder htmlBuilder, String style, ObjectResponseDTO object) {
        htmlBuilder.append(buildObjectDivOpenTag(object, style)).append(DIV);
    }

    private String buildObjectDivOpenTag(ObjectResponseDTO object, String style) {
        StringBuilder tagBuilder = new StringBuilder("<div class='object'");
        String objectId = object != null ? object.getId() : null;
        if (objectId != null && !objectId.isBlank()) {
            String escapedObjectId = escapeHtmlAttribute(objectId);
            tagBuilder.append(" id='").append(escapedObjectId).append("'");
            tagBuilder.append(" data-object-id='").append(escapedObjectId).append("'");
        }

        tagBuilder.append(" style='").append(style != null ? style : "").append("'>");
        return tagBuilder.toString();
    }

    private String escapeHtmlAttribute(String value) {
        return value
                .replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private double cmToPt(double cm) {
        final double CM_TO_PT = 28.3465;
        return cm * CM_TO_PT;
    }

}