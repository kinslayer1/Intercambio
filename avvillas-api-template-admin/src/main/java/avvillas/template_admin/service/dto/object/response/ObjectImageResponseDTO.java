package avvillas.template_admin.service.dto.object.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ObjectImageResponseDTO extends ObjectResponseDTO {
    private String image;
}
