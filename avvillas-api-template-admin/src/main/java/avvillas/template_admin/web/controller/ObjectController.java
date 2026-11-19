package avvillas.template_admin.web.controller;

import avvillas.template_admin.constant.message.CommonMessage;
import avvillas.template_admin.constant.message.ObjectMessage;
import avvillas.template_admin.service.ObjectService;
import avvillas.template_admin.service.dto.PatchUpdateResponse;
import avvillas.template_admin.service.dto.object.patch.PatchObjectBaseDTO;
import avvillas.template_admin.service.dto.object.request.ObjectBaseDTO;
import avvillas.template_admin.service.dto.object.response.ObjectPageResponseDTO;
import avvillas.template_admin.service.dto.object.response.ObjectResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/objects")
@RequiredArgsConstructor
public class ObjectController {

    private final ObjectService objectService;

    @PostMapping
    public ResponseEntity<ApiResponse<ObjectResponseDTO>> create(
            @Valid @RequestBody ObjectBaseDTO object) {
        ObjectResponseDTO created = objectService.create(object);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, ObjectMessage.OBJECT_CREATED, null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ObjectResponseDTO>> getById(@PathVariable String id) {
        ObjectResponseDTO response = objectService.getById(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(response, CommonMessage.QUERY_SUCCESS, null));
    }

    @GetMapping("/product/{id}")
    public ResponseEntity<ApiResponse<List<ObjectResponseDTO>>> getByProductId(@PathVariable String id) {
        List<ObjectResponseDTO> response = objectService.getByProductId(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(response, CommonMessage.QUERY_SUCCESS, null));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<ObjectResponseDTO>> update(@PathVariable String id, @Valid @RequestBody PatchObjectBaseDTO object) {
        PatchUpdateResponse<ObjectResponseDTO> response = objectService.update(id, object);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success(response.getUpdatedEntity(), ObjectMessage.OBJECT_UPDATED, response.getDifferences()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteById(@PathVariable String id) {
        objectService.deleteById(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(null, ObjectMessage.OBJECT_DELETED, null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ObjectPageResponseDTO>>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ObjectPageResponseDTO> products = objectService.searchGlobal(search, PageRequest.of(page, size));
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(products, CommonMessage.QUERY_SUCCESS, null));
    }

}