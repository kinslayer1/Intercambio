package avvillas.template_admin.persistence.mapper;

import avvillas.template_admin.persistence.entity.FormatEntity;
import avvillas.template_admin.service.dto.format.FormatResponseDto;
import org.springframework.stereotype.Component;

@Component
public class PdfConfigMapper {

    public FormatResponseDto.PdfConfigResponse toPdfConfigResponse(FormatEntity entity) {
        FormatResponseDto.PdfConfigResponse pdfConfig = new FormatResponseDto.PdfConfigResponse();
        pdfConfig.setContinues(entity.getContinues());
        pdfConfig.setPaperType(entity.getPaperType());
        pdfConfig.setFontFamily(entity.getFontFamily());

        FormatResponseDto.Margins margins = new FormatResponseDto.Margins();
        margins.setTop(entity.getTopMargin());
        margins.setBottom(entity.getBottomMargin());
        margins.setLeft(entity.getLeftMargin());
        margins.setRight(entity.getRightMargin());
        pdfConfig.setMargins(margins);

        return pdfConfig;
    }
}