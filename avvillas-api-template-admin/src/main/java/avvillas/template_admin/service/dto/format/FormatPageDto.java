package avvillas.template_admin.service.dto.format;

import avvillas.template_admin.service.dto.object.response.ObjectResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FormatPageDto {
    private String id;
    private int pageNumber;
    private List<ObjectResponseDTO> objects;
}
