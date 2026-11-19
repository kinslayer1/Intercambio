package avvillas.template_admin.service.dto.object.patch;

import avvillas.template_admin.constant.message.ObjectMessage;
import avvillas.template_admin.validation.annotation.OptionalNotEmpty;
import avvillas.template_admin.validation.annotation.OptionalSize;
import lombok.AccessLevel;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;
import java.util.Optional;

@Data
@NoArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
        property = ObjectMessage.OBJECT_TYPE,
        visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = PatchObjectTableDTO.class, name = ObjectMessage.TABLE_TYPE),
        @JsonSubTypes.Type(value = PatchObjectGenericDTO.class, name = ObjectMessage.GENERIC_TYPE),
        @JsonSubTypes.Type(value = PatchObjectImageDTO.class, name = ObjectMessage.IMAGE_TYPE)
})
public abstract class PatchObjectBaseDTO {
    private Optional<String> productId = Optional.empty();

    @OptionalNotEmpty(message = ObjectMessage.NAME_REQUIRED)
    @OptionalSize(min = 1, max = 100, message = ObjectMessage.NAME_MIN_MAX)
    private Optional<String> name = Optional.empty();

    @OptionalNotEmpty(message = ObjectMessage.IDENTIFIER_REQUIRED)
    @OptionalSize(min = 1, max = 10, message = ObjectMessage.IDENTIFIER_MIN_MAX)
    private Optional<String> identifier = Optional.empty();
    private Optional<Map<String, Object>> style = Optional.empty();
    private Optional<String> type = Optional.empty();
    @Setter(AccessLevel.NONE)
    private Optional<Boolean> esCierreTabla = Optional.empty();
    private Optional<Boolean> renderCondicionalHabilitado = Optional.empty();
    private Optional<String> renderCondicionalCampoId = Optional.empty();
    private Optional<String> renderCondicionalOperador = Optional.empty();
    private Optional<String> renderCondicionalValorEsperado = Optional.empty();

    @JsonIgnore
    private boolean esCierreTablaSpecified = false;

    @NotNull(message = ObjectMessage.OBJECT_TYPE_REQUIRED)
    private String objectType;

    @JsonSetter("esCierreTabla")
    public void setEsCierreTablaFromJson(Boolean value) {
        this.esCierreTabla = Optional.ofNullable(value);
        this.esCierreTablaSpecified = true;
    }

    public void setEsCierreTabla(Optional<Boolean> value) {
        this.esCierreTabla = value == null ? Optional.empty() : value;
        this.esCierreTablaSpecified = true;
    }

    @AssertTrue(message = ObjectMessage.TABLE_CLOSURE_REQUIRED)
    @JsonIgnore
    public boolean isEsCierreTablaValidWhenSpecified() {
        return !esCierreTablaSpecified || esCierreTabla.isPresent();
    }

    @AssertTrue(message = ObjectMessage.CONDITIONAL_RENDER_OPERATOR_INVALID)
    @JsonIgnore
    public boolean isConditionalRenderOperatorValidWhenSpecified() {
        return renderCondicionalOperador.isEmpty()
                || ObjectMessage.CONDITIONAL_RENDER_OPERATOR_EQUALS.equalsIgnoreCase(renderCondicionalOperador.get());
    }
}