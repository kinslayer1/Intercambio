package avvillas.template_admin.web.controller.format;

import avvillas.template_admin.constant.message.CommonMessage;
import avvillas.template_admin.service.FormatService;
import avvillas.template_admin.service.dto.format.FormatPageResponseDto;
import avvillas.template_admin.service.dto.format.FormatResponseDto;
import avvillas.template_admin.service.dto.product.ProductCodeDescriptionDTO;
import avvillas.template_admin.web.controller.ApiResponse;
import avvillas.template_admin.web.controller.FormatController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FormatController Unit Tests")
class FormatControllerTest {

    @Mock
    private FormatService formatService;

    @InjectMocks
    private FormatController formatController;

    private List<FormatPageResponseDto> formatList;
    private Page<FormatPageResponseDto> formatPage;

    @BeforeEach
    void setUp() {
        formatList = new ArrayList<>();

        ProductCodeDescriptionDTO product1 = new ProductCodeDescriptionDTO("prod-id-1", "P001", "Product One");
        ProductCodeDescriptionDTO product2 = new ProductCodeDescriptionDTO("prod-id-2", "P002", "Product Two");

        FormatPageResponseDto format1 = new FormatPageResponseDto(
                product1, "format-id-1", "prod-id-1", true, 1, null, null);

        FormatPageResponseDto format2 = new FormatPageResponseDto(
                product2, "format-id-2", "prod-id-2", true, 1, null, null);

        formatList.add(format1);
        formatList.add(format2);

        formatPage = new PageImpl<>(formatList, PageRequest.of(0, 10), formatList.size());
    }

    @Test
    @DisplayName("GET /formats - Get All Formats (No Search Term)")
    void whenNoSearchTerm_thenReturnAllFormats() {
        when(formatService.searchGlobal(eq(null), any(PageRequest.class))).thenReturn(formatPage);

        ResponseEntity<ApiResponse<Page<FormatPageResponseDto>>> response =
                formatController.getAll(null, 0, 10);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(CommonMessage.QUERY_SUCCESS, response.getBody().getMessage().get(0));
        assertEquals(formatPage, response.getBody().getData());
        assertEquals(2, response.getBody().getData().getTotalElements());
    }

    @Test
    @DisplayName("GET /formats - Search By Product")
    void whenSearchByProduct_thenReturnMatchingFormats() {
        Page<FormatPageResponseDto> filteredPage = new PageImpl<>(
                List.of(formatList.get(0)), PageRequest.of(0, 10), 1);

        when(formatService.searchGlobal(eq("Product One"), any(PageRequest.class))).thenReturn(filteredPage);

        ResponseEntity<ApiResponse<Page<FormatPageResponseDto>>> response =
                formatController.getAll("Product One", 0, 10);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(CommonMessage.QUERY_SUCCESS, response.getBody().getMessage().get(0));
        assertEquals(filteredPage, response.getBody().getData());
        assertEquals(1, response.getBody().getData().getTotalElements());
    }

    @Test
    @DisplayName("GET /formats - Pagination")
    void whenPaginationParameters_thenReturnPaginatedResults() {
        Page<FormatPageResponseDto> page0 = new PageImpl<>(
                List.of(formatList.get(0)), PageRequest.of(0, 1), formatList.size());

        Page<FormatPageResponseDto> page1 = new PageImpl<>(
                List.of(formatList.get(1)), PageRequest.of(1, 1), formatList.size());

        when(formatService.searchGlobal(null, PageRequest.of(0, 1))).thenReturn(page0);
        when(formatService.searchGlobal(null, PageRequest.of(1, 1))).thenReturn(page1);

        ResponseEntity<ApiResponse<Page<FormatPageResponseDto>>> response0 =
                formatController.getAll(null, 0, 1);

        assertNotNull(response0);
        assertEquals(HttpStatus.OK, response0.getStatusCode());
        assertNotNull(response0.getBody());
        assertEquals(CommonMessage.QUERY_SUCCESS, response0.getBody().getMessage().get(0));
        assertEquals(page0, response0.getBody().getData());
        assertEquals(1, response0.getBody().getData().getContent().size());
        assertEquals(0, response0.getBody().getData().getNumber());

        ResponseEntity<ApiResponse<Page<FormatPageResponseDto>>> response1 =
                formatController.getAll(null, 1, 1);

        assertNotNull(response1);
        assertEquals(HttpStatus.OK, response1.getStatusCode());
        assertNotNull(response1.getBody());
        assertEquals(CommonMessage.QUERY_SUCCESS, response1.getBody().getMessage().get(0));
        assertEquals(page1, response1.getBody().getData());
        assertEquals(1, response1.getBody().getData().getContent().size());
        assertEquals(1, response1.getBody().getData().getNumber());
    }

    @Test
    @DisplayName("Should return active format by product id")
    void whenGetActiveFormatByProductId_thenReturnFormat() {

        String productId = "123";

        FormatResponseDto mockResponse = new FormatResponseDto();
        mockResponse.setId("format-1");

        when(formatService.getActiveFormatByProductId(productId))
                .thenReturn(mockResponse);

        ResponseEntity<ApiResponse<FormatResponseDto>> response =
                formatController.getActiveFormatByProductId(productId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        assertNotNull(response.getBody());
        assertEquals(CommonMessage.QUERY_SUCCESS, response.getBody().getMessage().get(0));
        assertEquals(mockResponse, response.getBody().getData());

        verify(formatService).getActiveFormatByProductId(productId);
    }
}