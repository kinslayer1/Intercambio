package avvillas.template_admin.service.implementations;

import avvillas.template_admin.persistence.entity.FormatEntity;
import avvillas.template_admin.persistence.entity.FormatPageEntity;
import avvillas.template_admin.persistence.mapper.FormatMapper;
import avvillas.template_admin.persistence.mapper.FormatResponseMapper;
import avvillas.template_admin.persistence.mapper.ObjectMapper;
import avvillas.template_admin.persistence.repository.FormatRepository;
import avvillas.template_admin.persistence.repository.ObjectRepository;
import avvillas.template_admin.service.PdfService;
import avvillas.template_admin.service.dto.format.FormatDto;
import avvillas.template_admin.service.dto.format.FormatResponseDto;
import avvillas.template_admin.service.implementations.ObjectServiceImp.validator.ObjectValidator;
import avvillas.template_admin.web.traits.EntryTrait;
import avvillas.template_admin.web.traits.ProductTrait;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FormatServiceImpl Unit Tests")
class FormatServiceImplTest {

    @Mock
    private FormatRepository formatRepository;

    @Mock
    private ObjectRepository objectRepository;

    @Mock
    private ObjectValidator objectValidator;

    @Mock
    private EntryTrait entryTrait;

    @Mock
    private FormatMapper formatMapper;

    @Mock
    private FormatResponseMapper formatResponseMapper;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private ProductTrait productTrait;

    @Mock
    private PdfService pdfService;

    @InjectMocks
    private FormatServiceImpl formatService;

    private FormatDto formatDto;
    private FormatEntity existingFormat;

    @BeforeEach
    void setUp() {

        FormatDto.MarginDto margins = new FormatDto.MarginDto(
                BigDecimal.ONE,
                BigDecimal.ONE,
                BigDecimal.ONE,
                BigDecimal.ONE
        );

        FormatDto.PdfConfigDto pdfConfig = new FormatDto.PdfConfigDto(
                true,
                "A4",
                "Arial",
                margins
        );

        FormatDto.PageDto pageDto = new FormatDto.PageDto();
        pageDto.setPageNumber(1);
        pageDto.setElements(new ArrayList<>());
        pageDto.setFields(new ArrayList<>());

        formatDto = new FormatDto();
        formatDto.setProductId("product-1");
        formatDto.setPdfConfig(pdfConfig);
        formatDto.setPages(List.of(pageDto));

        existingFormat = new FormatEntity();
        existingFormat.setId("format-1");
        existingFormat.setProductId("product-1");

        existingFormat.setTopMargin(BigDecimal.ZERO);
        existingFormat.setBottomMargin(BigDecimal.ZERO);
        existingFormat.setLeftMargin(BigDecimal.ZERO);
        existingFormat.setRightMargin(BigDecimal.ZERO);

        existingFormat.setPaperType("LETTER");
        existingFormat.setFontFamily("Times");

        FormatPageEntity pageEntity = new FormatPageEntity();
        pageEntity.setId("page-1");
        pageEntity.setPageNumber(1);

        pageEntity.setObjects(new ArrayList<>());

        existingFormat.setPages(List.of(pageEntity));
    }

    @Test
    @DisplayName("Should update only pdf config when objects are the same")
    void shouldUpdateOnlyPdfConfigWhenObjectsAreEqual() {

        FormatResponseDto responseDto = new FormatResponseDto();
        responseDto.setId("format-1");

        when(formatRepository.findById("format-1"))
                .thenReturn(Optional.of(existingFormat));

        when(pdfService.generateHtmlFromDto(any(), any()))
                .thenReturn("<html>updated</html>");

        when(formatRepository.save(any(FormatEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(formatResponseMapper.toResponseDto(any(FormatEntity.class)))
                .thenReturn(responseDto);

        FormatResponseDto result = formatService.create(formatDto, "format-1");

        assertNotNull(result);
        assertEquals("format-1", result.getId());

        verify(objectValidator).validateProductExist("product-1");

        verify(pdfService).generateHtmlFromDto(any(), any());

        verify(formatRepository).save(existingFormat);

        assertEquals("<html>updated</html>", existingFormat.getHtmlContent());

        assertEquals(BigDecimal.ONE, existingFormat.getTopMargin());
        assertEquals(BigDecimal.ONE, existingFormat.getBottomMargin());
        assertEquals(BigDecimal.ONE, existingFormat.getLeftMargin());
        assertEquals(BigDecimal.ONE, existingFormat.getRightMargin());

        assertEquals("A4", existingFormat.getPaperType());
        assertEquals("Arial", existingFormat.getFontFamily());
    }
}
