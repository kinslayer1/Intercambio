package avvillas.template_admin.service.implementations.ObjectServiceImp.helpers;

import avvillas.template_admin.service.dto.object.TableHeaderConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.AllArgsConstructor;

@Converter(autoApply = true)
@AllArgsConstructor
public class HeaderConverter implements AttributeConverter<TableHeaderConfig, String> {

    private final ObjectMapper objectMapper;

    @Override
    public String convertToDatabaseColumn(TableHeaderConfig attribute) {
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error al serializar el header", e);
        }
    }

    @Override
    public TableHeaderConfig convertToEntityAttribute(String dbData) {
        try {
            return objectMapper.readValue(dbData, TableHeaderConfig.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error al deserializar el header", e);
        }
    }
}