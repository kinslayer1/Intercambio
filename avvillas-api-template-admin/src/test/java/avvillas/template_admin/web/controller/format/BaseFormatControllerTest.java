package avvillas.template_admin.web.controller.format;

import avvillas.template_admin.persistence.entity.ObjectEntity;
import avvillas.template_admin.persistence.repository.FormatObjectsRepository;
import avvillas.template_admin.persistence.repository.ObjectRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseFormatControllerTest {

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private ObjectRepository objectRepository;

    @Autowired
    private FormatObjectsRepository formatObjectsRepository;

    @Autowired
    protected RestTemplate restTemplate;

    protected MockRestServiceServer mockServer;

    protected static String createdFormatId;

    protected static String createdObjectId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockServer = MockRestServiceServer.createServer(restTemplate);

        formatObjectsRepository.deleteAll();
        objectRepository.deleteAll();
        ObjectEntity objectEntity = new ObjectEntity();
        objectEntity.setProductId(UUID.randomUUID().toString());
        objectEntity.setName("test-object");
        objectEntity.setIdentifier("object");
        objectEntity.setType("TEXT");
        objectEntity.setContent("Sample content");
        objectRepository.save(objectEntity);
        createdObjectId = objectEntity.getId();

    }

    protected String asJsonString(final Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}