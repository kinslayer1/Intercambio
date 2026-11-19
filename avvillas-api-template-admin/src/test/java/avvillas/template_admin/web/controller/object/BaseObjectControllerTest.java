package avvillas.template_admin.web.controller.object;

import avvillas.template_admin.persistence.repository.FormatObjectsRepository;
import avvillas.template_admin.persistence.repository.ObjectImageRepository;
import avvillas.template_admin.persistence.repository.ObjectRepository;
import avvillas.template_admin.persistence.repository.ObjectTableRepository;
import avvillas.template_admin.util.Utils;
import avvillas.template_admin.web.controller.object.Common.ObjectTestData;
import avvillas.template_admin.web.traits.ProductTrait;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public abstract class BaseObjectControllerTest {

    protected static final String OBJECT_ENDPOINT = "/objects";
    protected static final String OBJECT_ID_PATH = OBJECT_ENDPOINT + "/{id}";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected ObjectRepository objectRepository;

    @MockitoBean
    protected ProductTrait productTrait;

    protected ObjectTestData testData;

    @Autowired
    protected ObjectImageRepository imageRepository;

    @Autowired
    protected ObjectTableRepository tableRepository;

    @MockitoBean
    protected FormatObjectsRepository formatObjectsRepository;

    @BeforeEach
    void setUp() {
        objectRepository.deleteAll();
        testData = new ObjectTestData();
    }

    @AfterEach
    void tearDown() {
        objectRepository.deleteAll();
    }

    protected String toJson(Object obj) {
        return Utils.asJsonString(obj, objectMapper);
    }
}