package avvillas.template_admin.service.dto.format;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormatResponseDto {

    private String id;
    private String productId;
    private Boolean active;
    private Integer version;
    @SuppressWarnings("unused")
    private String htmlContent;
    @SuppressWarnings("unused")
    private String base64Content;
    private PdfConfigResponse pdfConfig;
    private List<PageResponse> pages;

    @Data
    public static class PdfConfigResponse {
        @SuppressWarnings("unused")
        private Boolean continues;
        private String paperType;
        private String fontFamily;
        private Margins margins;
    }

    @Data
    public static class Margins {
        @SuppressWarnings("unused")
        private BigDecimal top;
        @SuppressWarnings("unused")
        private BigDecimal left;
        @SuppressWarnings("unused")
        private BigDecimal right;
        @SuppressWarnings("unused")
        private BigDecimal bottom;
    }

    @Data
    public static class PageResponse {
        private int pageNumber;
        private List<ElementResponse> elements;
    }

    @Data
    public static class ElementResponse {
        private String fieldId;
        private String objectId;
        private int positionX;
        private int positionY;
        private Boolean esCierreTabla;
        private Boolean renderCondicionalHabilitado;
        private String renderCondicionalCampoId;
        private String renderCondicionalOperador;
        private String renderCondicionalValorEsperado;
    }
}