package avvillas.template_admin.web.traits;

import avvillas.template_admin.service.dto.input_product_structure.InputProductStructureDto;
import avvillas.template_admin.service.dto.product.ProductCodeDescriptionDTO;
import avvillas.template_admin.service.dto.product.ProductDTO;
import avvillas.template_admin.service.dto.util.PageResponse;
import avvillas.template_admin.web.controller.ApiResponse;
import lombok.AllArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class ProductTrait {

    private final CommunicationBetweenServices communication;

    public ProductDTO getProductById(String id) {
        String resource = "product/" + id;
        ResponseEntity<ApiResponse<ProductDTO>> response = communication.communicateWithMicroservice(
                "loader",
                resource,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );
        return Objects.requireNonNull(response.getBody()).getData();
    }

    public List<ProductCodeDescriptionDTO> searchProducts(String searchTerm) {
        String resource = "product?search=" + searchTerm + "&getAll=true";
        ResponseEntity<ApiResponse<PageResponse<ProductDTO>>> response = communication.communicateWithMicroservice(
                "loader",
                resource,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );
        return Objects.requireNonNull(response.getBody()).getData().getContent()
                .stream()
                .map(p -> new ProductCodeDescriptionDTO(p.getId(), p.getCode(), p.getDescription()))
                .collect(Collectors.toList());
    }

    public List<ProductCodeDescriptionDTO> getProductsByIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }

        String resource = "product/bulk";
        ResponseEntity<ApiResponse<List<ProductCodeDescriptionDTO>>> response = communication.communicateWithMicroservice(
                "loader",
                resource,
                HttpMethod.POST,
                ids,
                new ParameterizedTypeReference<>() {
                }
        );

        return Objects.requireNonNull(response.getBody()).getData();
    }

    public List<InputProductStructureDto> getInputProductStructureByIds(List<String> ids) {
        if (ids == null || ids.isEmpty())  return Collections.emptyList();

        String resource = "input-product-structure/bulk";
        ResponseEntity<ApiResponse<List<InputProductStructureDto>>> response = communication.communicateWithMicroservice(
                "loader",
                resource,
                HttpMethod.POST,
                ids,
                new ParameterizedTypeReference<>() {
                }
        );

        return Objects.requireNonNull(response.getBody()).getData();
    }
}
