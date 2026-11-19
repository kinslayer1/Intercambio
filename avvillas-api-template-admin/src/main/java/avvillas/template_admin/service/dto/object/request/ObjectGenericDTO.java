package avvillas.template_admin.service.dto.object.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ObjectGenericDTO extends ObjectBaseDTO {
    private String content;
}