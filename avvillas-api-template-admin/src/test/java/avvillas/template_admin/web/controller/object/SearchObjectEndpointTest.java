package avvillas.template_admin.web.controller.object;

import avvillas.template_admin.constant.message.CommonMessage;
import avvillas.template_admin.persistence.entity.ObjectEntity;
import avvillas.template_admin.service.dto.product.ProductCodeDescriptionDTO;
import avvillas.template_admin.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Object Controller - GET Endpoints")
class SearchObjectEndpointTest extends BaseObjectControllerTest {

    private static final String PRODUCT_ID_1 = "prod-uuid-1";
    private static final String PRODUCT_ID_2 = "prod-uuid-2";

    private void setupDatabaseWithTestData() {
        objectRepository.deleteAll();
        ObjectEntity obj1 = testData.createObjectEntity(PRODUCT_ID_1, "Generic Title", "TITLE");
        ObjectEntity obj2 = testData.createObjectEntity(PRODUCT_ID_1, "Generic Paragraph", "TEXT");
        ObjectEntity obj3 = testData.createObjectEntity(PRODUCT_ID_2, "Important Image", "IMAGE");
        objectRepository.saveAll(List.of(obj1, obj2, obj3));
    }

    @Nested
    @DisplayName("Endpoint: GET /objects/{id}")
    class GetById {
        @Test
        @DisplayName("Cuando el ID existe, debería retornar el objeto y status 200 OK")
        void whenIdExists_thenReturnObject() throws Exception {
            setupDatabaseWithTestData();
            ObjectEntity existingObject = objectRepository.findAll().get(0);

            mockMvc.perform(get(OBJECT_ID_PATH, existingObject.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message[0]", is(CommonMessage.QUERY_SUCCESS)))
                    .andExpect(jsonPath("$.data.id", is(existingObject.getId())))
                    .andExpect(jsonPath("$.data.name", is(existingObject.getName())));
        }

        @Test
        @DisplayName("Cuando el ID no existe, debería retornar 404 Not Found")
        void whenIdDoesNotExist_thenReturnNotFound() throws Exception {
            mockMvc.perform(get(OBJECT_ID_PATH, "non-existent-id"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Endpoint: GET /objects/product/{id}")
    class GetByProductId {
        @Test
        @DisplayName("Cuando el Product ID existe, debería retornar la lista de objetos asociados")
        void whenProductIdExists_thenReturnObjectList() throws Exception {

            setupDatabaseWithTestData();
            when(productTrait.getProductById(PRODUCT_ID_1)).thenReturn(TestDataBuilder.createMockProductDto());

            mockMvc.perform(get(OBJECT_ENDPOINT + "/product/{id}", PRODUCT_ID_1))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message[0]", is(CommonMessage.QUERY_SUCCESS)))
                    .andExpect(jsonPath("$.data", hasSize(2)));
        }

        @Test
        @DisplayName("Cuando el Product ID no tiene objetos, debería retornar una lista vacía")
        void whenProductIdHasNoObjects_thenReturnEmptyList() throws Exception {
            setupDatabaseWithTestData();
            when(productTrait.getProductById("prod-id-with-no-objects")).thenReturn(TestDataBuilder.createMockProductDto());
            
            mockMvc.perform(get(OBJECT_ENDPOINT + "/product/{id}", "prod-id-with-no-objects"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(0)));
        }

        @Test
        @DisplayName("Cuando el Product ID no existe, debería retornar 404 Not Found")
        void whenProductIdDoesNotExist_thenReturnNotFound() throws Exception {
            String nonExistentProductId = "non-existent-product-id";
            doThrow(new NoSuchElementException("Product not found")).when(productTrait).getProductById(nonExistentProductId);

            mockMvc.perform(get(OBJECT_ENDPOINT + "/product/{id}", nonExistentProductId))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Endpoint: GET /objects (Global Search)")
    class GlobalSearch {

        @BeforeEach
        void mockProductTrait() {

            ProductCodeDescriptionDTO product1 = new ProductCodeDescriptionDTO(PRODUCT_ID_1, "P01", "Producto Genérico");
            ProductCodeDescriptionDTO product2 = new ProductCodeDescriptionDTO(PRODUCT_ID_2, "P02", "Producto de Imagen");
            when(productTrait.getProductsByIds(anyList())).thenReturn(List.of(product1, product2));
        }

        @Test
        @DisplayName("Cuando no hay término de búsqueda, debería retornar todos los objetos paginados")
        void whenNoSearchTerm_thenReturnAllObjects() throws Exception {
            setupDatabaseWithTestData();
            ObjectEntity objectWithClosure = objectRepository.findAll().get(0);
            objectWithClosure.setEsCierreTabla(Boolean.TRUE);
            objectRepository.save(objectWithClosure);

            mockMvc.perform(get(OBJECT_ENDPOINT))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalElements", is(3)))
                .andExpect(jsonPath("$.data.content", hasSize(3)))
                    .andExpect(jsonPath("$.data.content[*].esCierreTabla", hasItem(true)));
        }

        @Test
        @DisplayName("Cuando se busca por un nombre de objeto local, debería retornar los resultados correctos")
        void whenSearchByLocalObjectName_thenReturnMatchingResults() throws Exception {

            setupDatabaseWithTestData();
            String searchTerm = "Generic";

            when(productTrait.searchProducts(searchTerm)).thenReturn(Collections.emptyList());

            mockMvc.perform(get(OBJECT_ENDPOINT).param("search", searchTerm))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalElements", is(2)))
                    .andExpect(jsonPath("$.data.content[0].name", is("Generic Title")))
                    .andExpect(jsonPath("$.data.content[1].name", is("Generic Paragraph")));
        }

        @Test
        @DisplayName("Cuando se busca por un término que coincide con un producto externo, debería filtrar por Product ID")
        void whenSearchMatchesExternalProduct_thenFilterByProductId() throws Exception {

            setupDatabaseWithTestData();
            String searchTerm = "Imagen";

            ProductCodeDescriptionDTO foundProduct = new ProductCodeDescriptionDTO(PRODUCT_ID_2, "P02", "Producto de Imagen");
            when(productTrait.searchProducts(searchTerm)).thenReturn(List.of(foundProduct));

            mockMvc.perform(get(OBJECT_ENDPOINT).param("search", searchTerm))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalElements", is(1)))
                    .andExpect(jsonPath("$.data.content[0].name", is("Important Image")));
        }
    }
}