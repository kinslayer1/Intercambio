package avvillas.template_admin.service.implementations;

import avvillas.template_admin.constant.PaperType;
import avvillas.template_admin.service.FormatService;
import avvillas.template_admin.service.dto.format.FormatDto;
import avvillas.template_admin.service.dto.format.FormatPageDto;
import avvillas.template_admin.service.dto.format.FormatPageResponseDto;
import avvillas.template_admin.service.dto.object.TableHeaderConfig;
import avvillas.template_admin.service.dto.object.response.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PdfServiceImpl Unit Tests")
class PdfServiceImplTest {

    @Mock
    private FormatService formatService;

    private PdfServiceImpl pdfServiceImpl;

    private FormatPageResponseDto formatDto;

    @BeforeEach
    void setUp() {

        pdfServiceImpl = new PdfServiceImpl(formatService);

        formatDto = new FormatPageResponseDto();

        FormatDto.MarginDto margins = new FormatDto.MarginDto(
                BigDecimal.valueOf(2.5),
                BigDecimal.valueOf(3.0),
                BigDecimal.valueOf(2.0),
                BigDecimal.valueOf(2.0)
        );

        FormatDto.PdfConfigDto pdfConfig =
                new FormatDto.PdfConfigDto(
                        true,
                        PaperType.A4,
                        "Arial",
                        margins
                );

        formatDto.setPdfConfig(pdfConfig);
        formatDto.setPages(new ArrayList<>());
    }

    @Test
    @DisplayName("Should generate pdf successfully")
    void shouldGeneratePdfSuccessfully() {

        String formatId = "format-123";

        Page<FormatPageResponseDto> page =
                new PageImpl<>(List.of(formatDto));

        when(formatService.searchGlobal(
                eq(formatId),
                eq(Pageable.unpaged())
        )).thenReturn(page);

        byte[] result = pdfServiceImpl.generatePdf(formatId);

        assertNotNull(result);
        assertTrue(result.length > 0);

        verify(formatService).searchGlobal(
                eq(formatId),
                eq(Pageable.unpaged())
        );

        verify(formatService).updateFormatContent(
                eq(formatId),
                anyString(),
                anyString()
        );
    }

    @Test
    @DisplayName("Should throw exception when format does not exist")
    void shouldThrowExceptionWhenFormatNotFound() {

        String formatId = "format-123";

        when(formatService.searchGlobal(
                eq(formatId),
                eq(Pageable.unpaged())
        )).thenReturn(Page.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> pdfServiceImpl.generatePdf(formatId)
        );

        verify(formatService).searchGlobal(
                eq(formatId),
                eq(Pageable.unpaged())
        );

        verify(formatService, never()).updateFormatContent(
                anyString(),
                anyString(),
                anyString()
        );
    }

    @Test
    @DisplayName("Should generate pdf with generic object")
    void shouldGeneratePdfWithGenericObject() {

        String formatId = "format-123";

        FormatPageDto page = new FormatPageDto();

        ObjectGenericResponseDTO object =
                new ObjectGenericResponseDTO();

        object.setType("shape");

        object.setStyle(Map.of(
                "width", "100px",
                "height", "20px"
        ));

        page.setObjects(List.of(object));

        formatDto.setPages(List.of(page));

        Page<FormatPageResponseDto> pageResponse =
                new PageImpl<>(List.of(formatDto));

        when(formatService.searchGlobal(
                eq(formatId),
                eq(Pageable.unpaged())
        )).thenReturn(pageResponse);

        byte[] result = pdfServiceImpl.generatePdf(formatId);

        assertNotNull(result);
        assertTrue(result.length > 0);

        verify(formatService).updateFormatContent(
                eq(formatId),
                anyString(),
                anyString()
        );
    }

    @Test
    @DisplayName("Should generate pdf with multiple pages")
    void shouldGeneratePdfWithMultiplePages() {
        String formatId = "format-123";

        FormatPageDto page1 = new FormatPageDto();
        page1.setPageNumber(1);
        page1.setObjects(List.of(new ObjectGenericResponseDTO()));

        FormatPageDto page2 = new FormatPageDto();
        page2.setPageNumber(2);
        page2.setObjects(List.of(new ObjectGenericResponseDTO()));

        formatDto.setPages(List.of(page1, page2));

        Page<FormatPageResponseDto> pageResponse = new PageImpl<>(List.of(formatDto));

        when(formatService.searchGlobal(
                eq(formatId),
                eq(Pageable.unpaged())
        )).thenReturn(pageResponse);

        byte[] result = pdfServiceImpl.generatePdf(formatId);

        assertNotNull(result);
        assertTrue(result.length > 0);

        ArgumentCaptor<String> htmlCaptor = ArgumentCaptor.forClass(String.class);
        verify(formatService).updateFormatContent(eq(formatId), htmlCaptor.capture(), anyString());

        String htmlContent = htmlCaptor.getValue();
        assertNotNull(htmlContent);
        assertEquals(2, htmlContent.split("<div class='page'>", -1).length - 1);
    }

    @Test
    @DisplayName("Should generate pdf with image object")
    void shouldGeneratePdfWithImageObject() {

        String formatId = "format-123";

        FormatPageDto page = new FormatPageDto();

        ObjectImageResponseDTO image =
                new ObjectImageResponseDTO();

        image.setImage("ZmFrZS1pbWFnZQ==");

        image.setStyle(Map.of(
                "width", "50px"
        ));

        page.setObjects(List.of(image));

        formatDto.setPages(List.of(page));

        Page<FormatPageResponseDto> pageResponse =
                new PageImpl<>(List.of(formatDto));

        when(formatService.searchGlobal(
                eq(formatId),
                eq(Pageable.unpaged())
        )).thenReturn(pageResponse);

        byte[] result = pdfServiceImpl.generatePdf(formatId);

        assertNotNull(result);
        assertTrue(result.length > 0);

        verify(formatService).updateFormatContent(
                eq(formatId),
                anyString(),
                anyString()
        );
    }

    @Test
    @DisplayName("Should generate pdf with table object")
    void shouldGeneratePdfWithTableObject() {

        String formatId = "format-123";

        TableHeaderConfig.ColumnConfig column =
                new TableHeaderConfig.ColumnConfig();

        column.setLabel("Nombre");

        column.setStyle(Map.of(
                "textAlign", "center"
        ));

        TableHeaderConfig header =
                new TableHeaderConfig();

        header.setColumns(List.of(column));

        CellResponseDTO cell =
                new CellResponseDTO();

        cell.setColumnIndex(0);
        cell.setContent("Juan");

        cell.setStyle(Map.of(
                "color", "red"
        ));

        TableBodyConfigResponse body =
                new TableBodyConfigResponse();

        body.setCells(List.of(cell));

        ObjectTableResponseDTO table =
                new ObjectTableResponseDTO();

        table.setHeader(header);
        table.setBody(body);

        FormatPageDto page = new FormatPageDto();

        page.setObjects(List.of(table));

        formatDto.setPages(List.of(page));

        Page<FormatPageResponseDto> pageResponse =
                new PageImpl<>(List.of(formatDto));

        when(formatService.searchGlobal(
                eq(formatId),
                eq(Pageable.unpaged())
        )).thenReturn(pageResponse);

        byte[] result = pdfServiceImpl.generatePdf(formatId);

        assertNotNull(result);
        assertTrue(result.length > 0);

        verify(formatService).updateFormatContent(
                eq(formatId),
                anyString(),
                anyString()
        );
    }

    @Test
    @DisplayName("Should build table html correctly")
    void shouldBuildTableHtmlCorrectly() throws Exception {

        TableHeaderConfig.ColumnConfig column1 = new TableHeaderConfig.ColumnConfig();
        column1.setLabel("Nombre");
        column1.setStyle(Map.of("textAlign", "center"));

        TableHeaderConfig.ColumnConfig column2 = new TableHeaderConfig.ColumnConfig();
        column2.setLabel("Edad");
        column2.setStyle(new HashMap<>());

        TableHeaderConfig header = new TableHeaderConfig();
        header.setColumns(List.of(column1, column2));

        CellResponseDTO cell1 = new CellResponseDTO();
        cell1.setColumnIndex(0);
        cell1.setContent("Juan");
        cell1.setStyle(Map.of("font-weight", "bold"));

        CellResponseDTO cell2 = new CellResponseDTO();
        cell2.setColumnIndex(1);
        cell2.setContent("30");
        cell2.setStyle(Map.of("text-align", "right"));

        TableBodyConfigResponse body = new TableBodyConfigResponse();
        body.setCells(List.of(cell1, cell2));

        ObjectTableResponseDTO table = new ObjectTableResponseDTO();
        table.setHeader(header);
        table.setBody(body);

        Method method = PdfServiceImpl.class
                .getDeclaredMethod("buildTableHtml", ObjectTableResponseDTO.class);

        method.setAccessible(true);

        String html = (String) method.invoke(pdfServiceImpl, table);

        assertNotNull(html);

        assertTrue(html.contains("<table>"));
        assertTrue(html.contains("<thead><tr>"));

        assertTrue(html.contains("Nombre"));
        assertTrue(html.contains("Edad"));

        assertTrue(html.contains("text-align: center"));
        assertTrue(html.contains("text-align: left"));

        assertTrue(html.contains("Juan"));
        assertTrue(html.contains("30"));

        assertTrue(html.contains("font-weight:bold;"));
        assertTrue(html.contains("text-align:right;"));

        assertTrue(html.contains("<tbody>"));
        assertTrue(html.contains("</table>"));
    }

    @Test
    @DisplayName("Should append shape html correctly")
    void shouldAppendShapeHtmlCorrectly() throws Exception {

        StringBuilder htmlBuilder = new StringBuilder();
        String style = "width:100px;height:100px";

        Method method = PdfServiceImpl.class
                .getDeclaredMethod("appendShapeHtml", StringBuilder.class, String.class);

        method.setAccessible(true);

        method.invoke(pdfServiceImpl, htmlBuilder, style);

        String result = htmlBuilder.toString();

        assertNotNull(result);
        assertTrue(result.contains("width:100px;height:100px"));
        assertTrue(result.contains("border: 1px solid transparent"));
        assertTrue(result.contains("</div>"));
    }

    @Test
    @DisplayName("Should append image html when object is image")
    void shouldAppendImageHtmlCorrectly() throws Exception {

        StringBuilder htmlBuilder = new StringBuilder();

        ObjectImageResponseDTO imageObject = new ObjectImageResponseDTO();

        imageObject.setImage(
                "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO7Z0ioAAAAASUVORK5CYII="
        );

        imageObject.setStyle(Map.of("width", "100px"));

        Method method = PdfServiceImpl.class
                .getDeclaredMethod(
                        "appendImageHtml",
                        StringBuilder.class,
                        String.class,
                        ObjectResponseDTO.class
                );

        method.setAccessible(true);

        method.invoke(pdfServiceImpl, htmlBuilder, "position:absolute;", imageObject);

        String result = htmlBuilder.toString();

        assertNotNull(result);
        assertTrue(result.contains("position:absolute;"));
        assertTrue(result.contains("<img"));
    }

    @Test
    @DisplayName("Should append error html when object is not image")
    void shouldAppendImageHtmlWithInvalidObject() throws Exception {

        StringBuilder htmlBuilder = new StringBuilder();

        ObjectGenericResponseDTO invalidObject = new ObjectGenericResponseDTO();

        Method method = PdfServiceImpl.class
                .getDeclaredMethod(
                        "appendImageHtml",
                        StringBuilder.class,
                        String.class,
                        ObjectResponseDTO.class
                );

        method.setAccessible(true);

        method.invoke(pdfServiceImpl, htmlBuilder, "position:absolute;", invalidObject);

        String result = htmlBuilder.toString();

        assertNotNull(result);
        assertTrue(result.contains("object"));
    }

    @Test
    @DisplayName("Should append bullet list correctly")
    void shouldAppendBulletListCorrectly() throws Exception {

        StringBuilder htmlBuilder = new StringBuilder();

        String style = "font-size:12px;";

        String content = """
            • Item 1
            • Item 2
            Texto normal
            • Item 3
            """;

        Method method = PdfServiceImpl.class
                .getDeclaredMethod(
                        "appendBulletList",
                        StringBuilder.class,
                        String.class,
                        String.class
                );

        method.setAccessible(true);

        method.invoke(pdfServiceImpl, htmlBuilder, style, content);

        String result = htmlBuilder.toString();

        assertNotNull(result);

        assertTrue(result.contains("<ul>"));
        assertTrue(result.contains("<li>Item 1</li>"));
        assertTrue(result.contains("<li>Item 2</li>"));
        assertTrue(result.contains("<li>Item 3</li>"));

        assertFalse(result.contains("Texto normal"));

        assertTrue(result.contains("font-size:12px;"));
        assertTrue(result.contains("</ul></div>"));
    }

    @Test
    @DisplayName("Should return correct font size delta")
    void shouldReturnCorrectFontSizeDelta() throws Exception {

        Method method = PdfServiceImpl.class
                .getDeclaredMethod("getFontSizeDelta", String.class);

        method.setAccessible(true);

        int result8 = (int) method.invoke(pdfServiceImpl, "font-size:8px;");
        int result9 = (int) method.invoke(pdfServiceImpl, "font-size:9px;");
        int result10 = (int) method.invoke(pdfServiceImpl, "font-size:10px;");
        int result11 = (int) method.invoke(pdfServiceImpl, "font-size:11px;");
        int defaultResult = (int) method.invoke(pdfServiceImpl, "font-size:15px;");
        int noFontResult = (int) method.invoke(pdfServiceImpl, "color:red;");

        assertEquals(9, result8);
        assertEquals(8, result9);
        assertEquals(9, result10);
        assertEquals(5, result11);

        assertEquals(6, defaultResult);
        assertEquals(6, noFontResult);
    }

    @Test
    @DisplayName("Should append table html correctly")
    void shouldAppendTableHtmlCorrectly() throws Exception {

        TableHeaderConfig.ColumnConfig column = new TableHeaderConfig.ColumnConfig();
        column.setLabel("Nombre");
        column.setStyle(Map.of("textAlign", "center"));

        TableHeaderConfig header = new TableHeaderConfig();
        header.setColumns(List.of(column));

        CellResponseDTO cell = new CellResponseDTO();
        cell.setColumnIndex(0);
        cell.setContent("Juan");
        cell.setStyle(Map.of("font-weight", "bold"));

        TableBodyConfigResponse body = new TableBodyConfigResponse();
        body.setCells(List.of(cell));

        ObjectTableResponseDTO table = new ObjectTableResponseDTO();
        table.setHeader(header);
        table.setBody(body);

        StringBuilder htmlBuilder = new StringBuilder();

        Method method = PdfServiceImpl.class
                .getDeclaredMethod(
                        "appendTableHtml",
                        StringBuilder.class,
                        String.class,
                        ObjectResponseDTO.class
                );

        method.setAccessible(true);

        method.invoke(
                pdfServiceImpl,
                htmlBuilder,
                "position:absolute;",
                table
        );

        String result = htmlBuilder.toString();

        assertNotNull(result);

        assertTrue(result.contains("position:absolute;"));

        assertTrue(result.contains("<table>"));
        assertTrue(result.contains("Nombre"));
        assertTrue(result.contains("Juan"));

        assertTrue(result.contains("font-weight:bold;"));

        assertTrue(result.contains("</table>"));
        assertTrue(result.contains("</div>"));
    }
}
