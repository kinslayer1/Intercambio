package avvillas.template_admin.persistence.mapper;

import avvillas.template_admin.persistence.entity.FormatEntity;
import avvillas.template_admin.persistence.entity.FormatPageEntity;
import avvillas.template_admin.service.dto.format.FormatResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FormatResponseMapper {

    private final PdfConfigMapper pdfConfigMapper;
    private final ElementMapper elementMapper;

    public FormatResponseDto toResponseDto(FormatEntity entity) {
        FormatResponseDto response = new FormatResponseDto();
        response.setId(entity.getId());
        response.setVersion(entity.getVersion());
        response.setProductId(entity.getProductId());
        response.setHtmlContent(entity.getHtmlContent());
        response.setBase64Content(entity.getBase64Content());
        response.setActive(entity.getActive() == 1);
        response.setPdfConfig(pdfConfigMapper.toPdfConfigResponse(entity));
        response.setPages(toPageResponses(entity));

        return response;
    }

    private List<FormatResponseDto.PageResponse> toPageResponses(FormatEntity entity) {
        if (entity.getPages() == null) return List.of();

        return entity.getPages().stream()
                .sorted(Comparator.comparingInt(FormatPageEntity::getPageNumber))
                .map(this::toPageResponse)
                .collect(Collectors.toList());
    }

    private FormatResponseDto.PageResponse toPageResponse(FormatPageEntity page) {
        FormatResponseDto.PageResponse pageResponse = new FormatResponseDto.PageResponse();
        pageResponse.setPageNumber(page.getPageNumber());

        if (page.getObjects() != null) {
            pageResponse.setElements(elementMapper.toElementResponses(page.getObjects()));
        } else {
            pageResponse.setElements(List.of());
        }

        return pageResponse;
    }
}