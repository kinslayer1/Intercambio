package avvillas.template_admin.service.dto.object.request;

import avvillas.template_admin.constant.message.ObjectMessage;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class CellDTO {
    private String id;

    @NotNull(message = ObjectMessage.ROW_INDEX_REQUIRED)
    private Integer rowIndex;

    @NotNull(message = ObjectMessage.COLUMN_INDEX_REQUIRED)
    private Integer columnIndex;

    private Integer colSpan;
    private Integer rowSpan;

    private String content;
    private Map<String, Object> style;
}
