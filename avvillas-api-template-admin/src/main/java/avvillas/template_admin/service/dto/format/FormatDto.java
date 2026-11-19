package avvillas.template_admin.service.dto.format;

import avvillas.template_admin.constant.message.FormatMessage;
import avvillas.template_admin.web.configuration.deserializer.StrictBooleanDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class FormatDto {
    private String id;

    @NotBlank(message = FormatMessage.PRODUCT_ID_REQUIRED)
    @Pattern(
            regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$",
            message = FormatMessage.PRODUCT_ID_INVALID
    )
    private String productId;

    @NotNull(message = FormatMessage.ACTIVE_REQUIRED)
    @JsonDeserialize(using = StrictBooleanDeserializer.class)
    private Boolean active;

    @NotNull(message = FormatMessage.PDF_CONFIG_REQUIRED)
    @Valid
    private PdfConfigDto pdfConfig;

    @NotEmpty(message = FormatMessage.PAGES_REQUIRED)
    @Valid
    private List<PageDto> pages;

    private LocalDateTime createdAt;
    private LocalDateTime updateAt;

    @Data
    @AllArgsConstructor
    public static class PdfConfigDto {
        @SuppressWarnings("unused")
        private Boolean continues;

        @NotBlank(message = FormatMessage.PAPER_TYPE_REQUIRED)
        private String paperType;

        @NotBlank(message = FormatMessage.FONT_FAMILY_REQUIRED)
        private String fontFamily;

        @NotNull(message = FormatMessage.MARGINS_REQUIRED)
        @Valid
        private MarginDto margins;
    }

    @Data
    public static class PageDto {
        @NotNull(message = FormatMessage.PAGE_NUMBER_REQUIRED)
        private Integer pageNumber;

        @NotEmpty(message = FormatMessage.ELEMENTS_REQUIRED)
        @Valid
        private List<ElementDto> elements;

        @NotEmpty(message = FormatMessage.FIELDS_REQUIRED)
        @Valid
        private List<FieldDto> fields;
    }

    @Data
    @AllArgsConstructor
    public static class MarginDto {
        @SuppressWarnings("unused")
        private BigDecimal top;
        @SuppressWarnings("unused")
        private BigDecimal bottom;
        @SuppressWarnings("unused")
        private BigDecimal left;
        @SuppressWarnings("unused")
        private BigDecimal right;
    }

    @Data
    public static class ElementDto {
        @NotBlank(message = FormatMessage.OBJECT_ID_ELEMENTS_REQUIRED)
        @Pattern(
                regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$",
                message = FormatMessage.OBJECT_ID_INVALID
        )
        private String objectId;

        @NotNull(message = FormatMessage.POSITION_X_ELEMENTS_REQUIRED)
        private int positionX;

        @NotNull(message = FormatMessage.POSITION_Y_ELEMENTS_REQUIRED)
        private int positionY;
    }

    @Data
    public static class FieldDto {
        @NotBlank(message = FormatMessage.FIELD_ID_FIELDS_REQUIRED)
        @Pattern(
                regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$",
                message = FormatMessage.FIELD_ID_INVALID
        )
        private String fieldId;

        @NotNull(message = FormatMessage.POSITION_X_FIELDS_REQUIRED)
        private int positionX;

        @NotNull(message = FormatMessage.POSITION_Y_FIELDS_REQUIRED)
        private int positionY;
    }
}