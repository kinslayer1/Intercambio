package avvillas.template_admin.web.controller;

import avvillas.template_admin.constant.message.PdfMessage;
import avvillas.template_admin.service.PdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pdf")
@RequiredArgsConstructor
public class PdfController {
    private final PdfService pdfService;

    @GetMapping(value = "/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> downloadPDFResource(@PathVariable("id") String formatId) {

        long startTime = System.currentTimeMillis();

        byte[] pdf = pdfService.generatePdf(formatId);

        long endTime = System.currentTimeMillis();

        long duration = endTime - startTime;

        int pdfSizeInBytes = pdf.length;

        double pdfSizeInKB = pdfSizeInBytes / 1024.0;
        double pdfSizeInMB = pdfSizeInBytes / (1024.0 * 1024.0);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("pdf", pdf);
        responseData.put("sizeInBytes", pdfSizeInBytes);
        responseData.put("sizeInKB", String.format("%.2f KB", pdfSizeInKB));
        responseData.put("sizeInMB", String.format("%.4f MB", pdfSizeInMB));
        responseData.put("generationTimeInMillis", duration + " ms");

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(responseData, PdfMessage.PDF_CREATED, null));

    }

}