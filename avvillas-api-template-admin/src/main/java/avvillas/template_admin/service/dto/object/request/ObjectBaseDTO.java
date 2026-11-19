package avvillas.template_admin.service.dto.object.request;

import avvillas.template_admin.constant.message.ObjectMessage;
import avvillas.template_admin.constant.message.ProductMessage;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Map;

@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
        property = ObjectMessage.OBJECT_TYPE,
        visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ObjectTableDTO.class, name = ObjectMessage.TABLE_TYPE),
        @JsonSubTypes.Type(value = ObjectImageDTO.class, name = ObjectMessage.IMAGE_TYPE),
        @JsonSubTypes.Type(value = ObjectGenericDTO.class, name = ObjectMessage.GENERIC_TYPE)
})
public abstract class ObjectBaseDTO {

    @NotBlank(message = ProductMessage.PRODUCT_ID_REQUIRED)
    private String productId;

    @NotBlank(message = ObjectMessage.NAME_REQUIRED)
    @Size(min = 1, max = 100, message = ObjectMessage.NAME_MIN_MAX)
    private String name;

    @NotBlank(message = ObjectMessage.IDENTIFIER_REQUIRED)
    @Size(min = 1, max = 10, message = ObjectMessage.IDENTIFIER_MIN_MAX)
    private String identifier;

    @NotBlank(message = ObjectMessage.TYPE_REQUIRED)
    @Pattern(regexp = ObjectMessage.ALLOWED_TYPES, message = ObjectMessage.ALLOWED_TYPES_READABLE)
    private String type;
    
    @NotNull(message = ObjectMessage.OBJECT_TYPE_REQUIRED)
    private String objectType;

    @NotNull(message = ObjectMessage.TABLE_CLOSURE_REQUIRED)
    private Boolean esCierreTabla;

        @NotNull(message = "El campo 'renderCondicionalHabilitado' es obligatorio.")
        private Boolean renderCondicionalHabilitado;

        private String renderCondicionalCampoId;

        private String renderCondicionalOperador;

        private String renderCondicionalValorEsperado;

    private Map<String, Object> style;
}