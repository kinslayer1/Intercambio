package avvillas.template_admin.service.dto.format;

import avvillas.template_admin.service.dto.object.response.ObjectResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FormatObjectsDto extends ObjectResponseDTO {
    private String id;
    private String fieldId;
    private Integer positionX;
    private Integer positionY;
    private String type;
    private String fieldName;
    private String typeRegister;
}
