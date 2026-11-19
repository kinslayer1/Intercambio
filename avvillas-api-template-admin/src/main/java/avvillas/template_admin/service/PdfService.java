package avvillas.template_admin.service;

import avvillas.template_admin.service.dto.format.FormatDto;
import avvillas.template_admin.service.dto.format.FormatPageDto;

import java.util.List;

public interface PdfService {

    byte[] generatePdf(String id);

    String generateHtmlFromDto(FormatDto.PdfConfigDto pdfConfig, List<FormatPageDto> pages);

}