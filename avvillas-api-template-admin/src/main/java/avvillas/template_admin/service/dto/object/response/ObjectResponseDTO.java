package avvillas.template_admin.service.dto.object.response;

import avvillas.template_admin.constant.message.ObjectMessage;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

import java.util.Map;

@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = ObjectMessage.TYPE)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ObjectTableResponseDTO.class, name = ObjectMessage.TABLE_TYPE),
        @JsonSubTypes.Type(value = ObjectGenericResponseDTO.class, name = ObjectMessage.TEXT_TYPE),
        @JsonSubTypes.Type(value = ObjectGenericResponseDTO.class, name = ObjectMessage.SHAPE_TYPE),
        @JsonSubTypes.Type(value = ObjectImageResponseDTO.class, name = ObjectMessage.IMAGE_TYPE)
})
public abstract class ObjectResponseDTO {
    private String id;
    private String productId;
    private String name;
    private String identifier;
    private String type;
    private Boolean esCierreTabla;
    private Boolean renderCondicionalHabilitado;
    private String renderCondicionalCampoId;
    private String renderCondicionalOperador;
    private String renderCondicionalValorEsperado;
    private Integer positionX;
    private Integer positionY;
    private Map<String, Object> style;
}
