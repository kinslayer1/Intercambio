package avvillas.template_admin.web.controller;

import avvillas.template_admin.constant.message.CommonMessage;
import avvillas.template_admin.constant.message.FormatMessage;
import avvillas.template_admin.service.FormatService;
import avvillas.template_admin.service.dto.format.FormatDto;
import avvillas.template_admin.service.dto.format.FormatPageResponseDto;
import avvillas.template_admin.service.dto.format.FormatResponseDto;
import avvillas.template_admin.service.dto.format.LastActiveFormatDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/formats")
@RequiredArgsConstructor
public class FormatController {

    private final FormatService formatService;

    @PostMapping
    public ResponseEntity<ApiResponse<FormatResponseDto>> create(@RequestParam(value = "id", required = false) String id,
                                                                 @Valid @RequestBody FormatDto formatDto) {
        FormatResponseDto createdFormat = formatService.create(formatDto, id);

        ApiResponse<FormatResponseDto> successResponse = ApiResponse.success(createdFormat, Objects.equals(id, createdFormat.getId()) ? FormatMessage.FORMAT_UPDATED : FormatMessage.FORMAT_CREATED, null);
        return ResponseEntity.status(HttpStatus.OK).body(successResponse);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<FormatResponseDto>> toggleFormatActivation(@PathVariable String id) {
        FormatResponseDto response = formatService.toggleFormatActivation(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(response, FormatMessage.FORMAT_ACTIVE, null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FormatResponseDto>> getById(@PathVariable String id) {
        FormatResponseDto response = formatService.getById(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(response, CommonMessage.QUERY_SUCCESS, null));
    }

    @GetMapping("/last-active/{id}")
    public ResponseEntity<ApiResponse<LastActiveFormatDTO>> getLastActiveFormatByProduct(@PathVariable String id) {
        LastActiveFormatDTO response = formatService.getLastActiveFormatByProduct(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(response, CommonMessage.QUERY_SUCCESS, null));
    }

    @GetMapping("/product/{id}")
    public ResponseEntity<ApiResponse<FormatResponseDto>> getFormatByProductId(@PathVariable String id) {
        FormatResponseDto response = formatService.getByProductId(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(response, CommonMessage.QUERY_SUCCESS, null));
    }

    @GetMapping("/active-format-by-product/{id}")
    public ResponseEntity<ApiResponse<FormatResponseDto>> getActiveFormatByProductId(@PathVariable String id) {
        FormatResponseDto response = formatService.getActiveFormatByProductId(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(response, CommonMessage.QUERY_SUCCESS, null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<FormatPageResponseDto>>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<FormatPageResponseDto> products = formatService.searchGlobal(search, PageRequest.of(page, size));
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(products, CommonMessage.QUERY_SUCCESS, null));
    }

}