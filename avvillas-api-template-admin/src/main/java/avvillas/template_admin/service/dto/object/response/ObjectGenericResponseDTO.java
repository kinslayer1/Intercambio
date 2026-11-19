package avvillas.template_admin.service.dto.object.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ObjectGenericResponseDTO extends ObjectResponseDTO {
    private String content;
}
