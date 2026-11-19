package avvillas.template_admin.service.dto.object.response;

import avvillas.template_admin.service.dto.product.ProductCodeDescriptionDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ObjectPageResponseDTO {
    private ProductCodeDescriptionDTO product;
    private String id;
    private String name;
    private String identifier;
    private Boolean esCierreTabla;
}
