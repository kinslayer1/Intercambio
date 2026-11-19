package avvillas.template_admin.web.controller.format;

import avvillas.template_admin.constant.message.CommonMessage;
import avvillas.template_admin.constant.message.FormatMessage;
import avvillas.template_admin.service.dto.format.FormatDto;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Format Controller - Search Endpoint Test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FormatSearchEndpointTest extends BaseFormatControllerTest {

    private static final String PRODUCT_ID = "7f63ac30-25ee-44f1-9845-6bd82f3ef985";
    private static final String PRODUCT_ID_2 = "7f63ac30-25ee-44f1-9845-6bd82f3ef986";
    private static final String FIELD_ID = "b0996f11-5786-47a9-90ab-833eadd83b70";
    private static final String LOADER_PATH = "/loader/product/";
    private static final String INPUT_PRODUCT_STRUCTURE_PATH = "/loader/input-product-structure/";
    private static final BigDecimal MARGIN_SIZE = BigDecimal.valueOf(2.45);
    private static final String PAPER_TYPE = "A4";
    private static final String FONT_FAMILY = "Arial";
    private static final int ELEMENT_POSITION_X = 100;
    private static final int ELEMENT_POSITION_Y = 200;
    private static final int FIELD_POSITION_X = 50;
    private static final int FIELD_POSITION_Y = 60;

    @Value("${gateway.url}")
    private String gatewayUrl;

    @BeforeEach
    @Override
    void setUp() {
        super.setUp();

        try {
            setupProductMock(PRODUCT_ID);
            setupProductStructureMock();
            FormatDto request1 = createFormatRequest(PRODUCT_ID);
            String formatId1 = createNewFormat(request1);
            assertNotNull(formatId1, "Format ID 1 should not be null");

            mockServer = MockRestServiceServer.createServer(restTemplate);

            setupProductMock(PRODUCT_ID_2);
            setupProductStructureMock();
            FormatDto request2 = createFormatRequest(PRODUCT_ID_2);
            String formatId2 = createNewFormat(request2);
            assertNotNull(formatId2, "Format ID 2 should not be null");

            mockServer = MockRestServiceServer.createServer(restTemplate);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set up test data", e);
        }
    }

    private void setupProductMock(String productId) {
        String apiUrl = gatewayUrl + LOADER_PATH + productId;
        String mockResponse = """
                {
                    "data": {
                        "id": "%s",
                        "code": "0398",
                        "description": "Product %s",
                        "documentType": "prueba",
                        "active": true,
                        "createdAt": "2025-07-10T22:26:30.952908",
                        "updateAt": "2025-07-11T22:47:02.740225"
                    },
                    "message": [
                        "Consulta exitosa"
                    ],
                    "service": "LOADER",
                    "log_entries": null
                }
                """.formatted(productId, productId.substring(0, 5));

        mockServer.expect(requestTo(apiUrl))
                .andRespond(withSuccess(
                        mockResponse,
                        MediaType.APPLICATION_JSON
                ));
    }

    private void setupProductStructureMock() {
        String apiUrl = gatewayUrl + INPUT_PRODUCT_STRUCTURE_PATH + FIELD_ID;
        String mockResponse = """
                {
                    "data": {
                        "id": "%s",
                        "productId": "7f63ac30-25ee-44f1-9845-6bd82f3ef982",
                        "typeRegister": "REG1",
                        "RegistrationIdentifier": "1",
                        "fieldName": "NombreProducto",
                        "indexFileIdentifier": null,
                        "initialPosition": "216",
                        "endPosition": "236",
                        "createdAt": "2025-07-10T22:26:30.952908",
                        "updateAt": "2025-07-11T22:47:02.740225"
                    },
                    "message": [
                        "Consulta exitosa"
                    ],
                    "service": "LOADER",
                    "log_entries": null
                }
                """.formatted(FIELD_ID);

        mockServer.expect(requestTo(apiUrl))
                .andRespond(withSuccess(
                        mockResponse,
                        MediaType.APPLICATION_JSON
                ));
    }

    private FormatDto createFormatRequest(String productId) {
        FormatDto request = new FormatDto();
        request.setProductId(productId);
        request.setActive(true);

        FormatDto.MarginDto margins = new FormatDto.MarginDto(
                MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE);

        FormatDto.PdfConfigDto pdfConfig = new FormatDto.PdfConfigDto(
                false, PAPER_TYPE, FONT_FAMILY, margins);

        request.setPdfConfig(pdfConfig);

        FormatDto.PageDto pageDto = new FormatDto.PageDto();
        pageDto.setPageNumber(1);

        FormatDto.ElementDto elementDto = new FormatDto.ElementDto();
        elementDto.setObjectId(createdObjectId);
        elementDto.setPositionX(ELEMENT_POSITION_X);
        elementDto.setPositionY(ELEMENT_POSITION_Y);
        pageDto.setElements(List.of(elementDto));

        FormatDto.FieldDto fieldDto = new FormatDto.FieldDto();
        fieldDto.setFieldId(FIELD_ID);
        fieldDto.setPositionX(FIELD_POSITION_X);
        fieldDto.setPositionY(FIELD_POSITION_Y);
        pageDto.setFields(List.of(fieldDto));

        request.setPages(List.of(pageDto));

        return request;
    }

    private String createNewFormat(FormatDto request) throws Exception {
        final String[] formatId = new String[1];

        try {
            mockMvc.perform(post("/formats")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.active").value(request.getActive()))
                    .andExpect(jsonPath("$.message[0]").value(FormatMessage.FORMAT_CREATED))
                    .andExpect(jsonPath("$.service").value("TEMPLATE_ADMIN"))
                    .andDo(result -> {
                        formatId[0] = JsonPath.read(result.getResponse().getContentAsString(), "$.data.id");
                    });

            mockServer.verify();
        } finally {
            mockServer = MockRestServiceServer.createServer(restTemplate);
        }

        return formatId[0];
    }

    @Test
    @Order(1)
    @DisplayName("GET /formats - Get All Formats (No Search Term)")
    void whenNoSearchTerm_thenReturnAllFormats() throws Exception {
        try {
            String bulkApiUrl = gatewayUrl + "/loader/product/bulk";
            String mockBulkResponse = """
                    {
                        "data": [],
                        "message": [
                            "Consulta exitosa"
                        ],
                        "service": "LOADER",
                        "log_entries": null
                    }
                    """;

            String structureBulkApiUrl = gatewayUrl + "/loader/input-product-structure/bulk";
            String mockStructureBulkResponse = """
                    {
                        "data": [],
                        "message": [
                            "Consulta exitosa"
                        ],
                        "service": "LOADER",
                        "log_entries": null
                    }
                    """;

            mockServer.expect(requestTo(bulkApiUrl))
                    .andRespond(withSuccess(
                            mockBulkResponse,
                            MediaType.APPLICATION_JSON
                    ));

            mockServer.expect(requestTo(structureBulkApiUrl))
                    .andRespond(withSuccess(
                            mockStructureBulkResponse,
                            MediaType.APPLICATION_JSON
                    ));

            mockMvc.perform(get("/formats"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.totalElements").isNumber())
                    .andExpect(jsonPath("$.message[0]").value(CommonMessage.QUERY_SUCCESS))
                    .andExpect(jsonPath("$.service").value("TEMPLATE_ADMIN"));
        } finally {
            mockServer = MockRestServiceServer.createServer(restTemplate);
        }
    }

    @Test
    @Order(3)
    @DisplayName("GET /formats - Pagination")
    void whenPaginationParameters_thenReturnPaginatedResults() throws Exception {
        try {
            String bulkApiUrl = gatewayUrl + "/loader/product/bulk";
            String mockBulkResponse = """
                    {
                        "data": [],
                        "message": [
                            "Consulta exitosa"
                        ],
                        "service": "LOADER",
                        "log_entries": null
                    }
                    """;

            String structureBulkApiUrl = gatewayUrl + "/loader/input-product-structure/bulk";
            String mockStructureBulkResponse = """
                    {
                        "data": [],
                        "message": [
                            "Consulta exitosa"
                        ],
                        "service": "LOADER",
                        "log_entries": null
                    }
                    """;

            mockServer.expect(requestTo(bulkApiUrl))
                    .andRespond(withSuccess(
                            mockBulkResponse,
                            MediaType.APPLICATION_JSON
                    ));

            mockServer.expect(requestTo(structureBulkApiUrl))
                    .andRespond(withSuccess(
                            mockStructureBulkResponse,
                            MediaType.APPLICATION_JSON
                    ));

            mockMvc.perform(get("/formats")
                            .param("page", "0")
                            .param("size", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.size").value(1))
                    .andExpect(jsonPath("$.data.number").value(0))
                    .andExpect(jsonPath("$.message[0]").value(CommonMessage.QUERY_SUCCESS))
                    .andExpect(jsonPath("$.service").value("TEMPLATE_ADMIN"));

            mockServer = MockRestServiceServer.createServer(restTemplate);

            mockServer.expect(requestTo(bulkApiUrl))
                    .andRespond(withSuccess(
                            mockBulkResponse,
                            MediaType.APPLICATION_JSON
                    ));

            mockServer.expect(requestTo(structureBulkApiUrl))
                    .andRespond(withSuccess(
                            mockStructureBulkResponse,
                            MediaType.APPLICATION_JSON
                    ));

            mockMvc.perform(get("/formats")
                            .param("page", "1")
                            .param("size", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.size").value(1))
                    .andExpect(jsonPath("$.data.number").value(1))
                    .andExpect(jsonPath("$.message[0]").value(CommonMessage.QUERY_SUCCESS))
                    .andExpect(jsonPath("$.service").value("TEMPLATE_ADMIN"));
        } finally {
            mockServer = MockRestServiceServer.createServer(restTemplate);
        }
    }
}