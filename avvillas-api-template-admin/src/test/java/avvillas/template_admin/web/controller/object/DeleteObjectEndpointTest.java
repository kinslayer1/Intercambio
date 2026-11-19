package avvillas.template_admin.web.controller.object;

import avvillas.template_admin.constant.message.ObjectMessage;
import avvillas.template_admin.persistence.entity.CellEntity;
import avvillas.template_admin.persistence.entity.ObjectEntity;
import avvillas.template_admin.persistence.entity.ObjectImageEntity;
import avvillas.template_admin.persistence.entity.ObjectTableEntity;
import avvillas.template_admin.persistence.repository.CellRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("ContentIndexFile Controller - Delete Update Endpoint Tests")
class DeleteObjectEndpointTest extends BaseObjectControllerTest {

    @Autowired
    protected CellRepository cellRepository;

    @BeforeEach
    void localSetup() {
        when(formatObjectsRepository.existsByObjectId(anyString())).thenReturn(false);
    }

    private ResultActions performDeleteRequest(String id) throws Exception {
        return mockMvc.perform(delete(OBJECT_ID_PATH, id));
    }

    @Nested
    @DisplayName("Eliminación de objetos genéricos")
    class GenericObject {
        @Test
        @DisplayName("Debería eliminar un objeto genérico exitosamente")
        void whenDeletingGenericObject_thenSucceeds() throws Exception {

            ObjectEntity saved = objectRepository.save(testData.createNewGenericObjectEntity());

            performDeleteRequest(saved.getId())
                    .andExpectAll(
                            status().isOk(),
                            jsonPath("$.message[0]").value(ObjectMessage.OBJECT_DELETED)
                    );

            assertEquals(0, objectRepository.count());
        }
    }

    @Nested
    @DisplayName("Eliminación de objetos de tipo imagen")
    class ImageObject {
        @Test
        @DisplayName("Debería eliminar un objeto de imagen exitosamente")
        void whenDeletingImageObject_thenSucceeds() throws Exception {
            ObjectImageEntity saved = imageRepository.save(testData.createNewImageObjectEntity());

            performDeleteRequest(saved.getId())
                    .andExpectAll(
                            status().isOk(),
                            jsonPath("$.message[0]").value(ObjectMessage.OBJECT_DELETED)
                    );

            assertEquals(0, imageRepository.count());
        }
    }

    @Nested
    @DisplayName("Eliminación de objetos de tipo tabla")
    class TableObject {
        @Test
        @DisplayName("Debería eliminar una tabla y sus celdas dependientes exitosamente")
        void whenDeletingTableObject_thenSucceedsAndDeletesCells() throws Exception {

            ObjectTableEntity savedTable = tableRepository.save(testData.createNewTableObjectEntity());
            CellEntity cell1 = new CellEntity();
            cell1.setTableObject(savedTable);
            cellRepository.save(cell1);
            assertEquals(1, tableRepository.count());
            assertEquals(2, cellRepository.count());

            performDeleteRequest(savedTable.getId())
                    .andExpectAll(
                            status().isOk(),
                            jsonPath("$.message[0]").value(ObjectMessage.OBJECT_DELETED)
                    );

            assertEquals(0, tableRepository.count(), "Table should be deleted");
            assertEquals(0, cellRepository.count(), "Dependent cells should be deleted");
        }
    }

    @Nested
    @DisplayName("Escenarios de falla")
    class Failures {
        @Test
        @DisplayName("Cuando se elimina un objeto inexistente, debería retornar 404 Not Found")
        void whenDeletingNonExistentObject_thenReturnNotFound() throws Exception {
            performDeleteRequest("non-existent-id")
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Cuando se elimina un objeto que está en uso por un formato, debería retornar Bad Request")
        void whenObjectIsInUse_thenReturnBadRequest() throws Exception {
            ObjectEntity saved = objectRepository.save(testData.createNewGenericObjectEntity());

            when(formatObjectsRepository.existsByObjectId(saved.getId())).thenReturn(true);


            performDeleteRequest(saved.getId())
                    .andExpectAll(
                            status().isBadRequest(),
                            jsonPath("$.message[0]").value(ObjectMessage.OBJECT_EXIST_IN_FORMAT)
                    );

            assertEquals(1, objectRepository.count(), "Object should not have been deleted");
        }
    }
}