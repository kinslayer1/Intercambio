package avvillas.template_admin.web.controller.object;

import avvillas.template_admin.constant.MessageConstant;
import avvillas.template_admin.constant.message.ObjectMessage;
import avvillas.template_admin.constant.message.ProductMessage;
import avvillas.template_admin.persistence.entity.ObjectImageEntity;
import avvillas.template_admin.persistence.entity.ObjectTableEntity;
import avvillas.template_admin.service.dto.object.request.ObjectBaseDTO;
import avvillas.template_admin.service.dto.object.request.ObjectGenericDTO;
import avvillas.template_admin.service.dto.object.request.ObjectImageDTO;
import avvillas.template_admin.service.dto.object.request.ObjectTableDTO;
import avvillas.template_admin.util.TestDataBuilder;
import avvillas.template_admin.web.controller.object.Common.ObjectTestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("ContentIndexFile Controller - Create Endpoint Tests")
class CreateObjectEndpointTest extends BaseObjectControllerTest {

    private ResultActions performCreateRequest(ObjectBaseDTO dto) throws Exception {
        return mockMvc.perform(post(OBJECT_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(dto)));
    }

    @Test
    @DisplayName("Cuando el ProductId no existe, deberia retornar 404 Not Found")
    void whenProductDoesNotExist_thenReturnNotFound() throws Exception {
        ObjectGenericDTO createDto = testData.createNewGenericObjectDto();
        createDto.setProductId(ObjectTestData.NON_EXISTENT_PRODUCT_ID);
        String errorMessage = String.format(ProductMessage.PRODUCT_NOT_FOUND, createDto.getProductId());

        doThrow(new NoSuchElementException(errorMessage))
                .when(productTrait).getProductById(createDto.getProductId());


        performCreateRequest(createDto)
                .andExpectAll(
                        status().isNotFound(),
                        jsonPath("$.message[0]").value(errorMessage)
                );
        assertEquals(0, objectRepository.count());
    }

    @Test
    @DisplayName("Cuando se intenta enviar una instancia equivocada del tipo de objeto, deberia retornar 404 Not Found")
    void whenSentWrongObjectInstance_thenReturnNotFound() throws Exception {
        ObjectGenericDTO genericDto = testData.createNewGenericObjectDto();
        genericDto.setType(ObjectMessage.TABLE_TYPE);
        when(productTrait.getProductById(genericDto.getProductId())).thenReturn(TestDataBuilder.createMockProductDto());
        ObjectGenericDTO imageDto = testData.createNewGenericObjectDto();
        imageDto.setType(ObjectMessage.TABLE_TYPE);
        when(productTrait.getProductById(imageDto.getProductId())).thenReturn(TestDataBuilder.createMockProductDto());

        ObjectGenericDTO tableDto = testData.createNewGenericObjectDto();
        tableDto.setType(ObjectMessage.IMAGE_TYPE);
        when(productTrait.getProductById(tableDto.getProductId())).thenReturn(TestDataBuilder.createMockProductDto());

        performCreateRequest(genericDto)
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.message[0]").value(MessageConstant.format(ObjectMessage.VERIFY_FORMAT_DATA, genericDto.getType()))
                );

        performCreateRequest(imageDto)
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.message[0]").value(MessageConstant.format(ObjectMessage.VERIFY_FORMAT_DATA, imageDto.getType()))
                );

        performCreateRequest(tableDto)
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.message[0]").value(MessageConstant.format(ObjectMessage.VERIFY_FORMAT_DATA, tableDto.getType()))
                );
        assertEquals(0, objectRepository.count());
    }


    @Nested
    @DisplayName("Generic Object Tests")
    class GenericObject {
        @Test
        @DisplayName("Cuando se envia datos validos de objeto GENERIC, deberia crearlos correctamente")
        void whenValidGenericData_thenCreateSuccessfully() throws Exception {

            ObjectGenericDTO createDto = testData.createNewGenericObjectDto();
            when(productTrait.getProductById(createDto.getProductId())).thenReturn(TestDataBuilder.createMockProductDto());

            performCreateRequest(createDto)
                    .andExpectAll(
                            status().isCreated(),
                            jsonPath("$.data.id").isNotEmpty(),
                            jsonPath("$.data.type").value(createDto.getType()),
                            jsonPath("$.data.content").value(createDto.getContent()),
                            jsonPath("$.message[0]").value(ObjectMessage.OBJECT_CREATED)
                    );
            assertEquals(1, objectRepository.count());
        }

        @Test
        @DisplayName("Cuando se crea un objeto con esCierreTabla=true, deberia persistirse y devolverse en la respuesta")
        void whenCreateWithTableClosureTrue_thenPersistAndReturnValue() throws Exception {

            ObjectGenericDTO createDto = testData.createNewGenericObjectDto();
            createDto.setEsCierreTabla(Boolean.TRUE);
            when(productTrait.getProductById(createDto.getProductId())).thenReturn(TestDataBuilder.createMockProductDto());

            performCreateRequest(createDto)
                    .andExpectAll(
                            status().isCreated(),
                            jsonPath("$.data.esCierreTabla").value(true),
                            jsonPath("$.message[0]").value(ObjectMessage.OBJECT_CREATED)
                    );

            assertEquals(Boolean.TRUE, objectRepository.findAll().get(0).getEsCierreTabla());
        }

        @Test
        @DisplayName("Cuando se crea un objeto con esCierreTabla en null, deberia retornar 400 Bad Request")
        void whenCreateWithTableClosureNull_thenReturnBadRequest() throws Exception {

            ObjectGenericDTO createDto = testData.createNewGenericObjectDto();
            createDto.setEsCierreTabla(null);
            when(productTrait.getProductById(createDto.getProductId())).thenReturn(TestDataBuilder.createMockProductDto());

            performCreateRequest(createDto)
                    .andExpectAll(
                            status().isBadRequest(),
                            jsonPath("$.message[0]").value(ObjectMessage.TABLE_CLOSURE_REQUIRED)
                    );
        }

        @Test
        @DisplayName("Cuando se envia datos invalidos del objeto TEXT, deberia retornar un Error")
        void whenValidTextWithoutContent_thenReturnError() throws Exception {

            ObjectGenericDTO createDto = testData.createNewGenericObjectDto();
            createDto.setType(ObjectMessage.TEXT_TYPE);
            createDto.setContent("");
            when(productTrait.getProductById(createDto.getProductId())).thenReturn(TestDataBuilder.createMockProductDto());

            performCreateRequest(createDto)
                    .andExpectAll(
                            status().isBadRequest(),
                            jsonPath("$.message[0]").value(ObjectMessage.CONTENT_REQUIRED)
                    );
            assertEquals(0, objectRepository.count());
        }
    }

    @Nested
    @DisplayName("Image Object Tests")
    class ImageObject {

        @Test
        @DisplayName("Cuando se envian datos correctos para el objeto IMAGE, deberia crearlo correctamente")
        void whenValidImageData_thenCreateSuccessfully() throws Exception {

            ObjectImageDTO createDto = testData.createNewImageObjectDto();
            when(productTrait.getProductById(createDto.getProductId())).thenReturn(TestDataBuilder.createMockProductDto());

            performCreateRequest(createDto)
                    .andExpectAll(
                            status().isCreated(),
                            jsonPath("$.data.id").isNotEmpty(),
                            jsonPath("$.data.type").value("IMAGE"),
                            jsonPath("$.data.image").value(ObjectTestData.VALID_BASE64_IMAGE_PNG),
                            jsonPath("$.message[0]").value(ObjectMessage.OBJECT_CREATED)
                    );

            Optional<ObjectImageEntity> savedEntityOpt = imageRepository.findAll().stream().findFirst();
            assertTrue(savedEntityOpt.isPresent());

            ObjectImageEntity savedEntity = savedEntityOpt.get();
            byte[] expectedBytes = Base64.getDecoder().decode(ObjectTestData.VALID_BASE64_IMAGE_PNG.split(",")[1]);
            assertEquals("image/png", savedEntity.getMimeType());
            assertArrayEquals(expectedBytes, savedEntity.getImage());
        }

        @Test
        @DisplayName("Cuando no se envia imagen, deberia retornar 400 Bad Request")
        void whenImageIsEmpty_thenReturnBadRequest() throws Exception {
            ObjectImageDTO createDto = testData.createNewImageObjectDto();
            createDto.setImage(null);
            when(productTrait.getProductById(createDto.getProductId())).thenReturn(TestDataBuilder.createMockProductDto());

            performCreateRequest(createDto)
                    .andExpectAll(
                            status().isBadRequest(),
                            jsonPath("$.message[0]").value(ObjectMessage.IMAGE_REQUIRED)
                    );
        }

        @Test
        @DisplayName("Cuando la imagen supera el limite de tamaño base64, deberia retornar 400 Bad Request")
        void whenImageExceedsMaxLength_thenReturnBadRequest() throws Exception {
            ObjectImageDTO createDto = testData.createNewImageObjectDto();

            createDto.setImage("data:image/png;base64," + ("A".repeat(ObjectMessage.MAX_BASE64_LENGTH + 1)));

            when(productTrait.getProductById(createDto.getProductId()))
                    .thenReturn(TestDataBuilder.createMockProductDto());

            performCreateRequest(createDto)
                    .andExpectAll(
                            status().isBadRequest(),
                            jsonPath("$.message[0]").value(ObjectMessage.IMAGE_MAX_SIZE)
                    );
        }

        @Test
        @DisplayName("Cuando la imagen supera el limite de tamaño de bytes, deberia retornar 400 Bad Request")
        void whenImageExceedsMaxBytesLength_thenReturnBadRequest() throws Exception {
            ObjectImageDTO createDto = testData.createNewImageObjectDto();
            byte[] largeBytes = new byte[ObjectMessage.MAX_SIZE_BYTES + 1];
            Arrays.fill(largeBytes, (byte) 1);

            String base64Large = Base64.getEncoder().encodeToString(largeBytes);

            createDto.setImage("data:image/png;base64," + base64Large);

            when(productTrait.getProductById(createDto.getProductId()))
                    .thenReturn(TestDataBuilder.createMockProductDto());

            performCreateRequest(createDto)
                    .andExpectAll(
                            status().isBadRequest(),
                            jsonPath("$.message[0]").value(ObjectMessage.IMAGE_MAX_SIZE)
                    );
        }

        @Test
        @DisplayName("Cuando se envia un MIME type que no es soportado, deberia retornar 400 Bad Request")
        void whenUnsupportedMimeType_thenReturnBadRequest() throws Exception {
            ObjectImageDTO createDto = testData.createNewImageObjectDto();
            createDto.setImage(ObjectTestData.UNSUPPORTED_MIME_TYPE_IMAGE);
            when(productTrait.getProductById(createDto.getProductId())).thenReturn(TestDataBuilder.createMockProductDto());

            performCreateRequest(createDto)
                    .andExpectAll(
                            status().isBadRequest(),
                            jsonPath("$.message[0]").value(ObjectMessage.IMAGE_NOT_SUPPORTED_FORMAT)
                    );
        }

        @Test
        @DisplayName("Cuando se envia una Imagen en Base64 mal formada, deberia devolver 400 Bad Request")
        void whenMalformedBase64_thenReturnBadRequest() throws Exception {

            ObjectImageDTO createDto = testData.createNewImageObjectDto();
            createDto.setImage(ObjectTestData.MALFORMED_BASE64_IMAGE);
            when(productTrait.getProductById(createDto.getProductId())).thenReturn(TestDataBuilder.createMockProductDto());

            performCreateRequest(createDto)
                    .andExpectAll(
                            status().isBadRequest(),
                            jsonPath("$.message[0]").value(ObjectMessage.IMAGE_NOT_SUPPORTED_FORMAT)
                    );
        }
    }

    @Nested
    @DisplayName("Table Object Tests")
    class TableObject {

        @Test
        @DisplayName("Cuando se envian los Datos correctamente, deberia responder correctamente")
        void whenValidImageData_thenCreateSuccessfully() throws Exception {

            ObjectTableDTO createDto = testData.createNewTableObjectDto();
            when(productTrait.getProductById(createDto.getProductId())).thenReturn(TestDataBuilder.createMockProductDto());

            performCreateRequest(createDto)
                    .andExpectAll(
                            status().isCreated(),
                            jsonPath("$.data.id").isNotEmpty(),
                            jsonPath("$.data.type").value("TABLE"),
                            jsonPath("$.message[0]").value(ObjectMessage.OBJECT_CREATED)
                    );

            Optional<ObjectTableEntity> savedEntityOpt = tableRepository.findAll().stream().findFirst();
            assertTrue(savedEntityOpt.isPresent());
        }

        @Test
        @DisplayName("Cuando se envian celdas pero la prop Fixed en False, deberia retornar 400 Bad Request")
        void whenCellsAreSentButFixesIsFalse_thenReturnBadRequest() throws Exception {
            ObjectTableDTO createDto = testData.createNewTableObjectDto();
            createDto.setFixed(false);

            when(productTrait.getProductById(createDto.getProductId())).thenReturn(TestDataBuilder.createMockProductDto());

            performCreateRequest(createDto)
                    .andExpectAll(
                            status().isBadRequest(),
                            jsonPath("$.message[0]").value(ObjectMessage.TABLE_FIXED_CELLS_INVALID)
                    );
        }

        @Test
        @DisplayName("Cuando se envia el header con las columnas vacias, deberia retornar 400 Bad Request")
        void whenHeaderContainColumnsEmpty_thenReturnBadRequest() throws Exception {
            ObjectTableDTO createDto = testData.createNewTableObjectDto();
            createDto.getHeader().setColumns(Collections.emptyList());

            when(productTrait.getProductById(createDto.getProductId())).thenReturn(TestDataBuilder.createMockProductDto());

            performCreateRequest(createDto)
                    .andExpectAll(
                            status().isBadRequest(),
                            jsonPath("$.message[0]").value(ObjectMessage.HEADER_MIN)
                    );
        }

        @Test
        @DisplayName("Cuando se envia el body en null, deberia retornar 400 Bad Request")
        void whenBodyIsNull_thenReturnBadRequest() throws Exception {
            ObjectTableDTO createDto = testData.createNewTableObjectDto();
            createDto.setBody(null);

            when(productTrait.getProductById(createDto.getProductId())).thenReturn(TestDataBuilder.createMockProductDto());

            performCreateRequest(createDto)
                    .andExpectAll(
                            status().isBadRequest(),
                            jsonPath("$.message[0]").value(ObjectMessage.BODY_REQUIRED)
                    );
        }

        @Test
        @DisplayName("Cuando se senvia rowIndex y ColIndex Vacio, deberia retornar 400 Bad Request")
        void whenInBodySentColIndexAndRowIndexEmpty_thenReturnBadRequest() throws Exception {
            ObjectTableDTO createDto = testData.createNewTableObjectDto();
            createDto.getBody().getCells().get(0).setRowIndex(null);
            createDto.getBody().getCells().get(0).setColumnIndex(null);

            when(productTrait.getProductById(createDto.getProductId())).thenReturn(TestDataBuilder.createMockProductDto());

            performCreateRequest(createDto)
                    .andExpectAll(
                            status().isBadRequest(),
                            jsonPath("$.message", hasItem(ObjectMessage.ROW_INDEX_REQUIRED)),
                            jsonPath("$.message", hasItem(ObjectMessage.COLUMN_INDEX_REQUIRED))
                    );
        }
    }
}