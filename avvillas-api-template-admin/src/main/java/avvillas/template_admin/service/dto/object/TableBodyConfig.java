package avvillas.template_admin.service.dto.object;

import avvillas.template_admin.service.dto.object.request.CellDTO;
import jakarta.validation.Valid;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class TableBodyConfig {
    private Map<String, Object> globalStyles;
    @Valid
    private List<CellDTO> cells;
}
