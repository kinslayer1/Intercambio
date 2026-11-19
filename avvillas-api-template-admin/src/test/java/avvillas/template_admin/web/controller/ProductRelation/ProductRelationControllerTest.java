package avvillas.template_admin.web.controller.ProductRelation;

import avvillas.template_admin.constant.message.CommonMessage;
import avvillas.template_admin.persistence.entity.ObjectEntity;
import avvillas.template_admin.persistence.repository.ObjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class ProductRelationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectRepository objectRepository;

    private String relatedProductId;
    private String unrelatedProductId;

    @BeforeEach
    void setUp() {
        objectRepository.deleteAll();

        relatedProductId = UUID.randomUUID().toString();
        unrelatedProductId = UUID.randomUUID().toString();

        ObjectEntity objectEntity = new ObjectEntity();
        objectEntity.setProductId(relatedProductId);
        objectEntity.setName("test-object");
        objectEntity.setIdentifier("object");
        objectEntity.setType("TEXT");
        objectEntity.setContent("Sample content");
        objectRepository.save(objectEntity);
    }

    @Test
    @DisplayName("Debería retornar true cuando el producto está relacionado en la BD")
    void shouldReturnTrueWhenProductIsRelated() throws Exception {
        mockMvc.perform(get("/relations/product/{id}", relatedProductId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", is(true)))
                .andExpect(jsonPath("$.message[0]", is(CommonMessage.QUERY_SUCCESS)));
    }

    @Test
    @DisplayName("Debería retornar false cuando el producto NO está relacionado en la BD")
    void shouldReturnFalseWhenProductIsNotRelated() throws Exception {
        mockMvc.perform(get("/relations/product/{id}", unrelatedProductId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", is(false)))
                .andExpect(jsonPath("$.message[0]", is(CommonMessage.QUERY_SUCCESS)));
    }
}
