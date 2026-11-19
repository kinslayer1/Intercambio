package avvillas.template_admin.service;

import avvillas.template_admin.service.dto.format.FormatDto;
import avvillas.template_admin.service.dto.format.FormatPageResponseDto;
import avvillas.template_admin.service.dto.format.FormatResponseDto;
import avvillas.template_admin.service.dto.format.LastActiveFormatDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FormatService {

    FormatResponseDto create(FormatDto dto, String id);

    FormatResponseDto getById(String id);

    FormatResponseDto getByProductId(String id);

    FormatResponseDto toggleFormatActivation(String id);

    Page<FormatPageResponseDto> searchGlobal(String search, Pageable pageable);

    void updateFormatContent(String formatId, String htmlContent, String base64Content);

    LastActiveFormatDTO getLastActiveFormatByProduct(String productId);

    FormatResponseDto getActiveFormatByProductId(String productId);
}