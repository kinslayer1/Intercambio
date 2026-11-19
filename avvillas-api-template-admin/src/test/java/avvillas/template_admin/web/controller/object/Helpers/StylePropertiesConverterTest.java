package avvillas.template_admin.web.controller.object.Helpers;

import avvillas.template_admin.service.implementations.ObjectServiceImp.helpers.StylePropertiesConverter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StylePropertiesConverterTest {

    private final ObjectMapper mapper = mock(ObjectMapper.class);
    private final StylePropertiesConverter converter = new StylePropertiesConverter(mapper);

    @Test
    @DisplayName("convertToDatabaseColumn - debería lanzar IllegalArgumentException en error de serialización")
    void convertToDatabaseColumn_throwsException() throws Exception {
        Map<String, Object> properties = Map.of("key", "value");
        when(mapper.writeValueAsString(properties)).thenThrow(new RuntimeException("Simulated"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            converter.convertToDatabaseColumn(properties);
        });

        assertEquals("Error al serializar propiedades de estilo", ex.getMessage());
    }

    @Test
    @DisplayName("convertToEntityAttribute - debería lanzar IllegalArgumentException en error de deserialización")
    void convertToEntityAttribute_throwsException() throws Exception {
        String json = "{}";
        when(mapper.readValue(eq(json), any(TypeReference.class))).thenThrow(new RuntimeException("Simulated"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            converter.convertToEntityAttribute(json);
        });

        assertEquals("Error al deserializar propiedades de estilo", ex.getMessage());
    }

}
