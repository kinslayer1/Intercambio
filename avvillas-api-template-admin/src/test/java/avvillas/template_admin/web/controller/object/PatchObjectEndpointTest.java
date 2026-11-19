package avvillas.template_admin.web.controller.object;

import avvillas.template_admin.constant.MessageConstant;
import avvillas.template_admin.constant.message.ObjectMessage;
import avvillas.template_admin.constant.message.ProductMessage;
import avvillas.template_admin.persistence.entity.CellEntity;
import avvillas.template_admin.persistence.entity.ObjectEntity;
import avvillas.template_admin.persistence.entity.ObjectImageEntity;
import avvillas.template_admin.persistence.entity.ObjectTableEntity;
import avvillas.template_admin.persistence.repository.CellRepository;
import avvillas.template_admin.service.dto.object.TableBodyConfig;
import avvillas.template_admin.service.dto.object.TableHeaderConfig;
import avvillas.template_admin.service.dto.object.patch.PatchObjectBaseDTO;
import avvillas.template_admin.service.dto.object.patch.PatchObjectGenericDTO;
import avvillas.template_admin.service.dto.object.patch.PatchObjectImageDTO;
import avvillas.template_admin.service.dto.object.patch.PatchObjectTableDTO;
import avvillas.template_admin.service.dto.object.request.CellDTO;
import avvillas.template_admin.web.controller.object.Common.ObjectTestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("ContentIndexFile Controller - Patch Update Endpoint Tests")
class PatchObjectEndpointTest extends BaseObjectControllerTest {

    private ResultActions performPatchRequest(PatchObjectBaseDTO dto, String id) throws Exception {
        return mockMvc.perform(patch(OBJECT_ID_PATH, id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(dto)));
    }

        private ResultActions performRawPatchRequest(String payload, String id) throws Exception {
                return mockMvc.perform(patch(OBJECT_ID_PATH, id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload));
        }

    private ObjectEntity setupAndSaveGenericObject() {
        return objectRepository.save(testData.createNewGenericObjectEntity());
    }

    private ObjectImageEntity setupAndSaveImageObject() {
        return imageRepository.save(testData.createNewImageObjectEntity());
    }

    private ObjectTableEntity setupAndSaveTableObject() {
        return tableRepository.save(testData.createNewTableObjectEntity());
    }

    @Test
    @DisplayName("Cuando el ProductId no existe, deberia retornar 404 Not Found")
    void whenProductDoesNotExist_thenReturnNotFound() throws Exception {
        ObjectEntity saved = setupAndSaveGenericObject();

        PatchObjectGenericDTO patchDto = new PatchObjectGenericDTO();
        patchDto.setProductId(Optional.of(ObjectTestData.NON_EXISTENT_PRODUCT_ID));
        patchDto.setObjectType(ObjectMessage.GENERIC_TYPE);

        String errorMessage = String.format(ProductMessage.PRODUCT_NOT_FOUND, patchDto.getProductId().get());

        doThrow(new NoSuchElementException(errorMessage))
                .when(productTrait).getProductById(patchDto.getProductId().get());


        performPatchRequest(patchDto, saved.getId())
                .andExpectAll(
                        status().isNotFound(),
                        jsonPath("$.message[0]").value(errorMessage)
                );
    }

    @Test
    @DisplayName("Cuando se intenta enviar una instancia equivocada del tipo de objeto, deberia retornar 400 Bad Request con mensaje claro")
    void whenSentWrongObjectInstance_thenReturnBadRequestWithMessage() throws Exception {

        ObjectEntity generic = setupAndSaveGenericObject();
        ObjectImageEntity image = setupAndSaveImageObject();

        final String IDENTIFIER = "TST";
        final String NAME = "TEST";

        PatchObjectTableDTO tableDtoWrongType = new PatchObjectTableDTO();
        tableDtoWrongType.setName(Optional.of(NAME));
        tableDtoWrongType.setIdentifier(Optional.of(IDENTIFIER + "1"));
        tableDtoWrongType.setObjectType(ObjectMessage.TABLE_TYPE);
        tableDtoWrongType.setType(Optional.of(ObjectMessage.IMAGE_TYPE));

        performPatchRequest(tableDtoWrongType, generic.getId())
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.message[0]").value(
                                MessageConstant.format(ObjectMessage.CHANGE_TYPE_REQUIRED_FULL, Optional.of(ObjectMessage.IMAGE_TYPE))
                        )
                );

        tableDtoWrongType.setObjectType(ObjectMessage.IMAGE_TYPE);
        tableDtoWrongType.setType(Optional.of(ObjectMessage.TEXT_TYPE));

        performPatchRequest(tableDtoWrongType, image.getId())
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.message[0]").value(
                                MessageConstant.format(ObjectMessage.CHANGE_TYPE_REQUIRED_FULL, Optional.of(ObjectMessage.TEXT_TYPE))
                        )
                );

        PatchObjectImageDTO imageDtoWrongType = new PatchObjectImageDTO();
        imageDtoWrongType.setName(Optional.of(NAME));
        imageDtoWrongType.setIdentifier(Optional.of(IDENTIFIER + "1"));
        imageDtoWrongType.setObjectType(ObjectMessage.IMAGE_TYPE);
        imageDtoWrongType.setType(Optional.of(ObjectMessage.TABLE_TYPE));

        performPatchRequest(imageDtoWrongType, image.getId())
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.message[0]").value(
                                MessageConstant.format(ObjectMessage.CHANGE_TYPE_REQUIRED_FULL, ObjectMessage.TABLE_TYPE)
                        )
                );
    }

    @Nested
    @DisplayName("Generic Object Tests")
    class GenericObject {

        private void assertGenericObjectContentIs(String objectId, String expectedContent) {
            ObjectEntity entity = objectRepository.findById(objectId)
                    .orElseThrow(() -> new AssertionError("Entity not found"));
            assertEquals(expectedContent, entity.getContent());
        }

        @Test
        @DisplayName("Cuando se envia datos validos de objeto GENERIC, deberia actualizarlo correctamente")
        void whenValidGenericData_thenCreateSuccessfully() throws Exception {

            String newContent = "NEW-TEXT";
            ObjectEntity saved = setupAndSaveGenericObject();
            PatchObjectGenericDTO patchDto = testData.createPatchWithNewContent(newContent);

            performPatchRequest(patchDto, saved.getId())
                    .andExpectAll(
                            status().isAccepted(),
                            jsonPath("$.data.id").isNotEmpty(),
                            jsonPath("$.data.type").value(saved.getType()),
                            jsonPath("$.data.content").value(newContent),
                            jsonPath("$.message[0]").value(ObjectMessage.OBJECT_UPDATED)
                    );

            assertGenericObjectContentIs(saved.getId(), newContent);
        }

        @Test
        @DisplayName("Cuando se envia datos invalidos del objeto TEXT, deberia retornar un Error")
        void whenValidTextWithoutContent_thenReturnError() throws Exception {

            ObjectEntity saved = setupAndSaveGenericObject();
            PatchObjectGenericDTO patchDto = testData.createPatchWithNewContent("");

            performPatchRequest(patchDto, saved.getId())
                    .andExpectAll(
                            status().isBadRequest(),
                            jsonPath("$.message[0]").value(ObjectMessage.CONTENT_REQUIRED)
                    );

            assertGenericObjectContentIs(saved.getId(), saved.getContent());
        }

        @Test
                @DisplayName("Cuando esCierreTabla llega explícitamente en null, debería retornar error y mantener el valor en base de datos")
                void whenEsCierreTablaIsExplicitNull_thenShouldReturnBadRequest() throws Exception {
            ObjectEntity saved = setupAndSaveGenericObject();
            saved.setEsCierreTabla(Boolean.TRUE);
            objectRepository.save(saved);

            String payload = """
                    {
                      "objectType": "GENERIC",
                      "esCierreTabla": null
                    }
                    """;

            performRawPatchRequest(payload, saved.getId())
                    .andExpectAll(
                            status().isBadRequest(),
                            jsonPath("$.message[0]").value(ObjectMessage.TABLE_CLOSURE_REQUIRED)
                    );

            ObjectEntity updated = objectRepository.findById(saved.getId())
                    .orElseThrow(() -> new AssertionError("Entity not found"));

                        assertEquals(Boolean.TRUE, updated.getEsCierreTabla());
        }

        @Test
        @DisplayName("Cuando se va a cambiar a Generic con datos incompletos del objeto TEXT, deberia retornar un Error")
        void whenObjectChangeTypeToGenericWithoutAllInformation_thenReturnError() throws Exception {

            ObjectImageEntity object = testData.createNewImageObjectEntity();
            ObjectImageEntity saved = imageRepository.save(object);

            PatchObjectGenericDTO patchDto = new PatchObjectGenericDTO();
            patchDto.setObjectType(ObjectMessage.GENERIC_TYPE);
            patchDto.setType(Optional.of(ObjectMessage.TEXT_TYPE));
            patchDto.setIdentifier(Optional.of("TEST"));
            patchDto.setName(Optional.of("TEST"));

            performPatchRequest(patchDto, saved.getId())
                    .andExpectAll(
                            status().isBadRequest(),
                            jsonPath("$.message[0]").value(String.format(ObjectMessage.CONTENT_REQUIRED_BY_TYPE, Optional.of(ObjectMessage.TEXT_TYPE)))
                    );

            Optional<ObjectEntity> savedEntityOpt = objectRepository.findAll().stream().findFirst();
            assertTrue(savedEntityOpt.isPresent());

            ObjectEntity savedEntity = savedEntityOpt.get();
            assertEquals(savedEntity.getType(), ObjectMessage.IMAGE_TYPE);
        }
    }

    @Nested
    @DisplayName("Image Object Tests")
    class ImageObject {

        private void assertImageContentIs(String objectId, byte[] expectedBytes) {
            ObjectImageEntity entity = imageRepository.findById(objectId)
                    .orElseThrow(() -> new AssertionError("Image entity not found"));
            assertArrayEquals(expectedBytes, entity.getImage());
        }

        @Test
        @DisplayName("Cuando se envian datos correctos para el objeto IMAGE, deberia crearlo correctamente")
        void whenValidImageData_thenCreateSuccessfully() throws Exception {

            String newImage = "data:image/png;base64,QUJD";
            ObjectEntity saved = setupAndSaveImageObject();
            PatchObjectImageDTO patchDto = testData.createPatchWithNewImage(newImage);

            performPatchRequest(patchDto, saved.getId())
                    .andExpectAll(
                            status().isAccepted(),
                            jsonPath("$.data.id").isNotEmpty(),
                            jsonPath("$.data.type").value("IMAGE"),
                            jsonPath("$.message[0]").value(ObjectMessage.OBJECT_UPDATED)
                    );

            assertImageContentIs(saved.getId(), "ABC".getBytes(StandardCharsets.UTF_8));
        }

        @Test
        @DisplayName("Cuando la imagen supera el limite de tamaño base64, deberia retornar 400 Bad Request")
        void whenImageExceedsMaxLength_thenReturnBadRequest() throws Exception {

            ObjectEntity saved = setupAndSaveImageObject();

            PatchObjectImageDTO patchDto = testData.createPatchWithNewImage(
                    "data:image/png;base64," + ("A".repeat(ObjectMessage.MAX_BASE64_LENGTH + 1)));

            performPatchRequest(patchDto, saved.getId())
                    .andExpectAll(
                            status().isBadRequest(),
                            jsonPath("$.message[0]").value(ObjectMessage.IMAGE_MAX_SIZE)
                    );
        }

        @Test
        @DisplayName("Cuando la imagen supera el limite de tamaño de bytes, deberia retornar 400 Bad Request")
        void whenImageExceedsMaxBytesLength_thenReturnBadRequest() throws Exception {

            ObjectEntity saved = setupAndSaveImageObject();

            byte[] largeBytes = new byte[ObjectMessage.MAX_SIZE_BYTES + 1];
            Arrays.fill(largeBytes, (byte) 1);

            String base64Large = Base64.getEncoder().encodeToString(largeBytes);

            PatchObjectImageDTO patchDto = testData.createPatchWithNewImage("data:image/png;base64," + base64Large);

            performPatchRequest(patchDto, saved.getId())
                    .andExpectAll(
                            status().isBadRequest(),
                            jsonPath("$.message[0]").value(ObjectMessage.IMAGE_MAX_SIZE)
                    );
        }

        @Test
        @DisplayName("Cuando se envia un MIME type que no es soportado, deberia retornar 400 Bad Request")
        void whenUnsupportedMimeType_thenReturnBadRequest() throws Exception {

            ObjectEntity saved = setupAndSaveImageObject();
            PatchObjectImageDTO patchDto = testData.createPatchWithNewImage(ObjectTestData.UNSUPPORTED_MIME_TYPE_IMAGE);

            performPatchRequest(patchDto, saved.getId())
                    .andExpectAll(
                            status().isBadRequest(),
                            jsonPath("$.message[0]").value(ObjectMessage.IMAGE_NOT_SUPPORTED_FORMAT)
                    );
        }

        @Test
        @DisplayName("Cuando se envia una Imagen en Base64 mal formada, deberia devolver 400 Bad Request")
        void whenMalformedBase64_thenReturnBadRequest() throws Exception {

            ObjectEntity saved = setupAndSaveImageObject();
            PatchObjectImageDTO patchDto = testData.createPatchWithNewImage(ObjectTestData.MALFORMED_BASE64_IMAGE);

            performPatchRequest(patchDto, saved.getId())
                    .andExpectAll(
                            status().isBadRequest(),
                            jsonPath("$.message[0]").value(ObjectMessage.IMAGE_NOT_SUPPORTED_FORMAT)
                    );
        }

        @Test
        @DisplayName("Cuando se cambia de TABLE a IMAGE con datos válidos, debería actualizar correctamente")
        void whenChangeTableToImage_thenUpdateSuccessfully() throws Exception {
            ObjectTableEntity object = testData.createNewTableObjectEntity();
            ObjectTableEntity saved = tableRepository.save(object);

            String newImage = "data:image/png;base64,QUJD";

            PatchObjectImageDTO patchDto = new PatchObjectImageDTO();
            patchDto.setObjectType(ObjectMessage.IMAGE_TYPE);
            patchDto.setType(Optional.of(ObjectMessage.IMAGE_TYPE));
            patchDto.setName(Optional.of(saved.getName()));
            patchDto.setIdentifier(Optional.of("test_1"));
            patchDto.setImage(Optional.of(newImage));

            performPatchRequest(patchDto, saved.getId())
                    .andExpectAll(
                            status().isAccepted(),
                            jsonPath("$.data.id").isNotEmpty(),
                            jsonPath("$.data.type").value(ObjectMessage.IMAGE_TYPE),
                            jsonPath("$.message[0]").value(ObjectMessage.OBJECT_UPDATED)
                    );

            List<ObjectImageEntity> allImages = imageRepository.findAll();
            ObjectImageEntity updated = allImages.get(allImages.size() - 1);
            assertEquals(ObjectMessage.IMAGE_TYPE, updated.getType());
            assertArrayEquals("ABC".getBytes(StandardCharsets.UTF_8), updated.getImage());
        }

        @Test
        @DisplayName("Cuando se cambia de TABLE a IMAGE sin enviar la informacion completa, debería retornar Bad Request")
        void whenChangeTableToImageWithoutImage_thenReturnBadRequest() throws Exception {
            ObjectTableEntity object = testData.createNewTableObjectEntity();
            ObjectTableEntity saved = tableRepository.save(object);

            PatchObjectImageDTO patchDto = new PatchObjectImageDTO();
            patchDto.setObjectType(ObjectMessage.IMAGE_TYPE);
            patchDto.setType(Optional.of(ObjectMessage.IMAGE_TYPE));
            patchDto.setName(Optional.of(saved.getName()));
            patchDto.setIdentifier(Optional.of("test_1"));

            performPatchRequest(patchDto, saved.getId())
                    .andExpectAll(
                            status().isBadRequest(),
                            jsonPath("$.message[0]").value(ObjectMessage.IMAGE_REQUIRED)
                    );

            ObjectEntity notChanged = objectRepository.findById(saved.getId()).orElseThrow();
            assertEquals(ObjectMessage.TABLE_TYPE, notChanged.getType());
        }
    }

    @Nested
    @DisplayName("Table Object Tests")
    class TableObject {

        @Autowired
        protected CellRepository cellRepository;

        @Test
        @DisplayName("Cuando se envían datos válidos, debería crear la tabla correctamente y persistir celdas")
        void whenValidTableData_thenCreateSuccessfully() throws Exception {

            ObjectTableEntity saved = setupAndSaveTableObject();
            String newCellContent = "New Cell Content";
            PatchObjectTableDTO patchDto = testData.createPatchWithNewCell(newCellContent);

            performPatchRequest(patchDto, saved.getId())
                    .andExpectAll(
                            status().isAccepted(),
                            jsonPath("$.data.id").value(saved.getId()),
                            jsonPath("$.data.type").value(ObjectMessage.TABLE_TYPE),
                            jsonPath("$.message[0]").value(ObjectMessage.OBJECT_UPDATED)
                    );

            ObjectTableEntity updated = tableRepository.findById(saved.getId()).get();
            assertAll(
                    () -> assertEquals(ObjectMessage.TABLE_TYPE, updated.getType()),
                    () -> assertFalse(updated.getHeader().getColumns().isEmpty())
            );

            List<CellEntity> cells = cellRepository.findByTableObjectId(saved.getId())
                    .orElseThrow();

            CellEntity updatedCell = cells.get(cells.size() - 1);

            assertEquals(newCellContent, updatedCell.getContent());
        }

        @Test
        @DisplayName("Cuando se envían celdas pero fixed es false, debería retornar error")
        void whenCellsSentButFixedIsFalse_thenReturnBadRequest() throws Exception {
            ObjectTableEntity saved = setupAndSaveTableObject();
            PatchObjectTableDTO patchDto = testData.createPatchWithNewCell("TEST");
            patchDto.setFixed(Optional.of(false));

            performPatchRequest(patchDto, saved.getId())
                    .andExpectAll(
                            status().isBadRequest(),
                            jsonPath("$.message[0]").value(ObjectMessage.TABLE_FIXED_CELLS_INVALID)
                    );
        }

        @Test
        @DisplayName("Cuando se envían celdas pero fixed en la entidad es false, debería retornar error")
        void whenCellsSentButEntityFixedIsFalse_thenReturnBadRequest() throws Exception {
            ObjectTableEntity entity = testData.createNewTableObjectEntity();
            entity.setFixed(0);
            ObjectTableEntity saved = tableRepository.save(entity);
            PatchObjectTableDTO patchDto = testData.createPatchWithNewCell("TEST");

            performPatchRequest(patchDto, saved.getId())
                    .andExpectAll(
                            status().isBadRequest(),
                            jsonPath("$.message[0]").value(ObjectMessage.TABLE_FIXED_CELLS_INVALID)
                    );
        }

        @Test
        @DisplayName("Cuando se envía header sin columnas, debería retornar error")
        void whenHeaderWithoutColumns_thenReturnBadRequest() throws Exception {
            ObjectTableEntity saved = setupAndSaveTableObject();
            PatchObjectTableDTO patchDto = testData.createPatchWithNewCell("TEST");

            TableHeaderConfig headerConfig = new TableHeaderConfig();
            headerConfig.setColumns(Collections.emptyList());
            patchDto.setHeader(Optional.of(headerConfig));

            performPatchRequest(patchDto, saved.getId())
                    .andExpectAll(
                            status().isBadRequest(),
                            jsonPath("$.message[0]").value(ObjectMessage.HEADER_MIN)
                    );
        }

        @Test
        @DisplayName("Cuando se envía cell con rowIndex y columnIndex vacíos, debería retornar error")
        void whenCellWithoutIndexes_thenReturnBadRequest() throws Exception {

            ObjectTableEntity saved = setupAndSaveTableObject();
            PatchObjectTableDTO patchDto = testData.createPatchWithNewCell("TEST");

            patchDto.setObjectType(ObjectMessage.TABLE_TYPE);

            TableBodyConfig bodyConfig = new TableBodyConfig();
            CellDTO cell = new CellDTO();
            cell.setContent("Celda sin índices");
            cell.setRowIndex(null);
            cell.setColumnIndex(null);
            bodyConfig.setCells(List.of(cell));

            patchDto.setBody(Optional.of(bodyConfig));

            performPatchRequest(patchDto, saved.getId())
                    .andExpectAll(
                            status().isBadRequest(),
                            jsonPath("$.message", hasItem(ObjectMessage.ROW_INDEX_REQUIRED)),
                            jsonPath("$.message", hasItem(ObjectMessage.COLUMN_INDEX_REQUIRED))
                    );
        }

        @Test
        @DisplayName("Cuando se actualiza una celda existente correctamente, debería persistir cambios")
        void whenUpdatingExistingCell_thenPersistChanges() throws Exception {
            ObjectTableEntity saved = setupAndSaveTableObject();
            CellEntity existingCell = saved.getCells().get(0);
            PatchObjectTableDTO patchDto = testData.createPatchWithNewCell("TEST");

            String newCellContent = "New Cell Content";
            patchDto.setObjectType(ObjectMessage.TABLE_TYPE);

            TableBodyConfig bodyConfig = new TableBodyConfig();
            CellDTO cell = new CellDTO();
            cell.setId(existingCell.getId());
            cell.setContent(newCellContent);
            cell.setRowIndex(existingCell.getRowIndex());
            cell.setColumnIndex(existingCell.getColumnIndex());
            bodyConfig.setCells(List.of(cell));

            patchDto.setBody(Optional.of(bodyConfig));

            performPatchRequest(patchDto, saved.getId())
                    .andExpectAll(
                            status().isAccepted(),
                            jsonPath("$.message[0]").value(ObjectMessage.OBJECT_UPDATED)
                    );

            CellEntity updatedCell = cellRepository.findByIdAndTableObjectId(existingCell.getId(), saved.getId())
                    .orElseThrow();

            assertEquals(newCellContent, updatedCell.getContent());
        }

        @Test
        @DisplayName("Cuando se actualiza una celda inexistente, debería retornar Bad Request")
        void whenUpdatingUnExistingCell_thenReturnBadRequest() throws Exception {
            ObjectTableEntity saved = setupAndSaveTableObject();
            CellEntity existingCell = saved.getCells().get(0);
            PatchObjectTableDTO patchDto = testData.createPatchWithNewCell("TEST");

            String newCellContent = "New Cell Content";
            patchDto.setObjectType(ObjectMessage.TABLE_TYPE);

            TableBodyConfig bodyConfig = new TableBodyConfig();
            CellDTO cell = new CellDTO();
            cell.setId("123");
            cell.setContent(newCellContent);
            cell.setRowIndex(existingCell.getRowIndex());
            cell.setColumnIndex(existingCell.getColumnIndex());
            bodyConfig.setCells(List.of(cell));

            patchDto.setBody(Optional.of(bodyConfig));

            performPatchRequest(patchDto, saved.getId())
                    .andExpectAll(
                            status().isBadRequest(),
                            jsonPath("$.message[0]").value(String.format(ObjectMessage.CELL_NOT_EXIST, cell.getId()))
                    );

            CellEntity updatedCell = cellRepository.findByIdAndTableObjectId(existingCell.getId(), saved.getId())
                    .orElseThrow();

            assertEquals(existingCell.getContent(), updatedCell.getContent());
        }

        @Test
        @DisplayName("Cuando se cambia de IMAGE a TABLE con datos válidos, debería actualizar correctamente")
        void whenChangeImageToTable_thenUpdateSuccessfully() throws Exception {
            ObjectImageEntity object = testData.createNewImageObjectEntity();
            ObjectImageEntity saved = imageRepository.save(object);

            PatchObjectTableDTO patchDto = new PatchObjectTableDTO();
            patchDto.setObjectType(ObjectMessage.TABLE_TYPE);
            patchDto.setType(Optional.of(ObjectMessage.TABLE_TYPE));
            patchDto.setFixed(Optional.of(true));

            TableHeaderConfig headerConfig = new TableHeaderConfig();
            TableHeaderConfig.ColumnConfig col1 = new TableHeaderConfig.ColumnConfig();
            col1.setLabel("Col 1");
            col1.setRowIndex(0);
            col1.setColumnIndex(0);
            headerConfig.setColumns(List.of(col1));
            patchDto.setHeader(Optional.of(headerConfig));

            TableBodyConfig bodyConfig = new TableBodyConfig();
            CellDTO cell = new CellDTO();
            cell.setContent("Celda tras cambio de tipo");
            cell.setRowIndex(0);
            cell.setColumnIndex(0);
            bodyConfig.setCells(List.of(cell));
            patchDto.setBody(Optional.of(bodyConfig));

            patchDto.setType(Optional.of(ObjectMessage.TABLE_TYPE));
            patchDto.setName(Optional.of(saved.getName()));
            patchDto.setIdentifier(Optional.of("test_1"));

            performPatchRequest(patchDto, saved.getId())
                    .andExpectAll(
                            status().isAccepted(),
                            jsonPath("$.data.id").isNotEmpty(),
                            jsonPath("$.data.type").value(ObjectMessage.TABLE_TYPE),
                            jsonPath("$.message[0]").value(ObjectMessage.OBJECT_UPDATED)
                    );

            List<ObjectTableEntity> allTables = tableRepository.findAll();
            ObjectTableEntity updated = allTables.get(allTables.size() - 1);
            assertEquals(ObjectMessage.TABLE_TYPE, updated.getType());
            assertFalse(updated.getHeader().getColumns().isEmpty());

            List<CellEntity> cells = cellRepository.findByTableObjectId(updated.getId())
                    .orElseThrow();

            CellEntity updatedCell = cells.get(cells.size() - 1);

            assertEquals("Celda tras cambio de tipo", updatedCell.getContent());
        }
    }
}