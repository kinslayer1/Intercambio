package avvillas.template_admin.web.controller.object.Helpers;

import avvillas.template_admin.service.dto.object.TableBodyConfig;
import avvillas.template_admin.service.implementations.ObjectServiceImp.helpers.BodyConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BodyConverterTest {
    private final ObjectMapper mapper = mock(ObjectMapper.class);
    private final BodyConverter converter = new BodyConverter(mapper);

    @Test
    @DisplayName("convertToDatabaseColumn - debería lanzar RuntimeException en error de serialización")
    void convertToDatabaseColumn_throwsException() throws Exception {
        TableBodyConfig config = new TableBodyConfig();
        when(mapper.writeValueAsString(config)).thenThrow(new RuntimeException("Simulated"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            converter.convertToDatabaseColumn(config);
        });

        assertEquals("Error converting TableBodyConfig to JSON", ex.getMessage());
    }

    @Test
    @DisplayName("convertToEntityAttribute - debería lanzar RuntimeException en error de deserialización")
    void convertToEntityAttribute_throwsException() throws Exception {
        String json = "{}";
        when(mapper.readValue(json, TableBodyConfig.class)).thenThrow(new RuntimeException("Simulated"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            converter.convertToEntityAttribute(json);
        });

        assertEquals("Error converting JSON to TableBodyConfig", ex.getMessage());
    }
}
