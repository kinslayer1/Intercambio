package avvillas.template_admin.service.dto.input_product_structure;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InputProductStructureDto {
    private String id;
    private String productId;
    private String typeRegister;
    private String fieldName;
}