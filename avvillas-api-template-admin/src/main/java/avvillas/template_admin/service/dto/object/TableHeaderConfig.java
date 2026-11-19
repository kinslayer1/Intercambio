package avvillas.template_admin.service.dto.object;

import avvillas.template_admin.constant.message.ObjectMessage;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class TableHeaderConfig {
    @NotEmpty(message = ObjectMessage.HEADER_MIN)
    @Valid
    private List<ColumnConfig> columns;

    private Map<String, Object> globalStyles;

    @Data
    public static class ColumnConfig {

        private String label;

        private Integer colSpan;
        private Integer rowSpan;
        
        @NotNull(message = ObjectMessage.ROW_INDEX_REQUIRED)
        private Integer rowIndex;

        @NotNull(message = ObjectMessage.COLUMN_INDEX_REQUIRED)
        private Integer columnIndex;

        private Map<String, Object> style;
    }
}
