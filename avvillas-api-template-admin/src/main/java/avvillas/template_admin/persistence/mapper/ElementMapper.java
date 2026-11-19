package avvillas.template_admin.persistence.mapper;

import avvillas.template_admin.persistence.entity.FormatObjectsEntity;
import avvillas.template_admin.service.dto.format.FormatResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ElementMapper {
    List<FormatResponseDto.ElementResponse> toElementResponses(List<FormatObjectsEntity> objects);

    @Mapping(target = "objectId", source = "object.id")
    @Mapping(target = "fieldId", source = "fieldId")
    @Mapping(target = "positionX", source = "positionX")
    @Mapping(target = "positionY", source = "positionY")
    @Mapping(target = "esCierreTabla", source = "object.esCierreTabla")
    @Mapping(target = "renderCondicionalHabilitado", source = "object.renderCondicionalHabilitado")
    @Mapping(target = "renderCondicionalCampoId", source = "object.renderCondicionalCampoId")
    @Mapping(target = "renderCondicionalOperador", source = "object.renderCondicionalOperador")
    @Mapping(target = "renderCondicionalValorEsperado", source = "object.renderCondicionalValorEsperado")
    FormatResponseDto.ElementResponse toElementResponse(FormatObjectsEntity object);

}
