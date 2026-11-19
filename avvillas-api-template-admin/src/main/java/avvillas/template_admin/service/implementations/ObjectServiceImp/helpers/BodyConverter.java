package avvillas.template_admin.service.implementations.ObjectServiceImp.helpers;

import avvillas.template_admin.service.dto.object.TableBodyConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.AllArgsConstructor;

@Converter
@AllArgsConstructor
public class BodyConverter implements AttributeConverter<TableBodyConfig, String> {
    private final ObjectMapper mapper;

    @Override
    public String convertToDatabaseColumn(TableBodyConfig attribute) {
        try {
            return mapper.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new RuntimeException("Error converting TableBodyConfig to JSON", e);
        }
    }

    @Override
    public TableBodyConfig convertToEntityAttribute(String dbData) {
        try {
            return mapper.readValue(dbData, TableBodyConfig.class);
        } catch (Exception e) {
            throw new RuntimeException("Error converting JSON to TableBodyConfig", e);
        }
    }
}