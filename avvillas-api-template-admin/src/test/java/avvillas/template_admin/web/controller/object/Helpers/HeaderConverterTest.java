package avvillas.template_admin.web.controller.object.Helpers;

import avvillas.template_admin.service.dto.object.TableHeaderConfig;
import avvillas.template_admin.service.implementations.ObjectServiceImp.helpers.HeaderConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HeaderConverterTest {

    private final ObjectMapper mapper = mock(ObjectMapper.class);
    private final HeaderConverter converter = new HeaderConverter(mapper);

    @Test
    @DisplayName("convertToDatabaseColumn - debería lanzar IllegalArgumentException en error de serialización")
    void convertToDatabaseColumn_throwsException() throws Exception {
        TableHeaderConfig config = new TableHeaderConfig();
        when(mapper.writeValueAsString(config)).thenThrow(new RuntimeException("Simulated"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            converter.convertToDatabaseColumn(config);
        });

        assertEquals("Error al serializar el header", ex.getMessage());
    }

    @Test
    @DisplayName("convertToEntityAttribute - debería lanzar IllegalArgumentException en error de deserialización")
    void convertToEntityAttribute_throwsException() throws Exception {
        String json = "{}";
        when(mapper.readValue(json, TableHeaderConfig.class)).thenThrow(new RuntimeException("Simulated"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            converter.convertToEntityAttribute(json);
        });

        assertEquals("Error al deserializar el header", ex.getMessage());
    }

}
