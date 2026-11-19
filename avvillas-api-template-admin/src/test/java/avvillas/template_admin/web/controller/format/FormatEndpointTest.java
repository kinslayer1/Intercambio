package avvillas.template_admin.web.controller.format;

import avvillas.template_admin.constant.MessageConstant;
import avvillas.template_admin.constant.message.CommonMessage;
import avvillas.template_admin.constant.message.FormatMessage;
import avvillas.template_admin.service.dto.format.FormatDto;
import com.jayway.jsonpath.JsonPath;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Format Controller - Endpoints Test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FormatEndpointTest extends BaseFormatControllerTest {

    private static final String PRODUCT_ID = "7f63ac30-25ee-44f1-9845-6bd82f3ef985";
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

    private void setupProductMock() {
        String apiUrl = gatewayUrl + LOADER_PATH + PRODUCT_ID;
        String mockResponse = """
                {
                    "data": {
                        "id": "%s",
                        "code": "0398",
                        "description": "prueba",
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
                """.formatted(PRODUCT_ID);

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

    private FormatDto createFormatRequest() {
        FormatDto request = new FormatDto();
        request.setProductId(PRODUCT_ID);
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

    @Test
    @Order(1)
    @DisplayName("POST /formats - Successful Creation")
    void whenValidRequest_thenCreateSuccessfully() throws Exception {
        setupProductMock();
        setupProductStructureMock();

        FormatDto request = createFormatRequest();

        mockMvc.perform(post("/formats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.active").value(request.getActive()))
                .andExpect(jsonPath("$.message[0]").value(FormatMessage.FORMAT_CREATED))
                .andExpect(jsonPath("$.service").value("TEMPLATE_ADMIN"))
                .andDo(result -> {
                    createdFormatId = JsonPath.read(result.getResponse().getContentAsString(), "$.data.id");
                });

        mockServer.verify();
    }

    @Test
    @Order(2)
    @DisplayName("POST /formats - Create Multiple Formats Successfully")
    void whenValidRequests_thenCreateMultipleFormatsSuccessfully() throws Exception {
        String formatId1 = createNewFormat();
        assertNotNull(formatId1, "El ID del primer formato no debería ser null");
    }

    @Test
    @DisplayName("GET /formats/{id} - Successful Retrieval")
    void whenValidId_thenReturnFormatSuccessfully() throws Exception {
        mockMvc.perform(get("/formats/{id}", createdFormatId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(createdFormatId))
                .andExpect(jsonPath("$.message[0]").value(CommonMessage.QUERY_SUCCESS))
                .andExpect(jsonPath("$.service").value("TEMPLATE_ADMIN"));
    }

    @Test
    @DisplayName("GET /formats/{id} - Format Not Found")
    void whenInvalidId_thenThrowNotFound() throws Exception {
        String invalidId = "non-existing-id";

        mockMvc.perform(get("/formats/{id}", invalidId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message[0]").value(String.format(FormatMessage.FORMAT_NOT_FOUND, invalidId)));
    }

    @Test
    @DisplayName("PUT /formats/{id} - Format Not Found")
    void whenInvalidIdPatch_thenThrowNotFound() throws Exception {
        String invalidId = "non-existing-id";

        mockMvc.perform(patch("/formats/{id}", invalidId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message[0]").value(String.format(FormatMessage.FORMAT_NOT_FOUND, invalidId)));
    }

    @Test
    @DisplayName("PUT /formats/{id} - Format Activation")
    void whenValidId_toggleActivationSuccessfully() throws Exception {

        mockMvc.perform(patch("/formats/{id}", createdFormatId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(createdFormatId))
                .andExpect(jsonPath("$.message[0]").value(FormatMessage.FORMAT_ACTIVE))
                .andExpect(jsonPath("$.service").value("TEMPLATE_ADMIN"));
    }

    private String createNewFormat() throws Exception {
        setupProductMock();
        setupProductStructureMock();

        FormatDto request = createFormatRequest();

        final String[] formatId = new String[1];

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

        return formatId[0];
    }

    @Test
    @Order(6)
    @DisplayName("GET /last-active/{id} - Debe devolver la versión cuando se encuentra un formato activo")
    void whenActiveFormatExists_thenLastActiveShouldReturnVersion() throws Exception {

        mockMvc.perform(get("/formats/last-active/{id}", PRODUCT_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.version").value(4))
                .andExpect(jsonPath("$.message[0]").value(CommonMessage.QUERY_SUCCESS))
                .andExpect(jsonPath("$.service").value("TEMPLATE_ADMIN"));
    }

    @Test
    @Order(7)
    @DisplayName("GET /last-active/{id} - Debe devolver data nula si el producto no tiene formatos")
    void whenProductHasNoFormats_thenLastActiveShouldReturnNullData() throws Exception {

        String nonExistentProductId = "producto-sin-formatos-999";

        mockMvc.perform(get("/formats/last-active/{id}", nonExistentProductId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.data").value(CoreMatchers.nullValue()))
                .andExpect(jsonPath("$.message").value(MessageConstant.format(FormatMessage.FORMAT_NOT_FOUND, nonExistentProductId)));
    }
}