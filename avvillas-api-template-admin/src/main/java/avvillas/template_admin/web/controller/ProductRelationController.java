package avvillas.template_admin.web.controller;

import avvillas.template_admin.constant.message.CommonMessage;
import avvillas.template_admin.service.ProductRelationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/relations/product")
@RequiredArgsConstructor
public class ProductRelationController {

    private final ProductRelationService productRelation;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> getProductRelated(@PathVariable String id) {
        Boolean response = productRelation.isProductRelated(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(response, CommonMessage.QUERY_SUCCESS, null));
    }


}