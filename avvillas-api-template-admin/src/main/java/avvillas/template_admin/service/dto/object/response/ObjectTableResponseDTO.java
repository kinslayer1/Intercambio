package avvillas.template_admin.service.dto.object.response;

import avvillas.template_admin.service.dto.object.TableHeaderConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ObjectTableResponseDTO extends ObjectResponseDTO {
    private TableHeaderConfig header;
    private Boolean fixed;
    private TableBodyConfigResponse body;
}
