package avvillas.template_admin.service.dto.object.response;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class TableBodyConfigResponse {
    private Map<String, Object> globalStyles;
    private List<CellResponseDTO> cells;
}
