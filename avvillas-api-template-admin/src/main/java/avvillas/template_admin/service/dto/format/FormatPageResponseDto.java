package avvillas.template_admin.service.dto.format;

import avvillas.template_admin.service.dto.product.ProductCodeDescriptionDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FormatPageResponseDto {

    private ProductCodeDescriptionDTO product;
    private String id;
    private String productId;
    private Boolean active;
    private Integer version;
    private FormatDto.PdfConfigDto pdfConfig;
    private List<FormatPageDto> pages;

}