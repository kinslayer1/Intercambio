package avvillas.template_admin.service.dto.object.response;

import lombok.Data;

import java.util.Map;

@Data
public class CellResponseDTO {
    private String id;
    private Integer rowIndex;
    private Integer columnIndex;
    private Integer colSpan;
    private Integer rowSpan;
    private String content;
    private Map<String, Object> style;
}
