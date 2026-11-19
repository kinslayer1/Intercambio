package avvillas.template_admin.persistence.mapper;


import avvillas.template_admin.persistence.entity.CellEntity;
import avvillas.template_admin.persistence.entity.ObjectEntity;
import avvillas.template_admin.persistence.entity.ObjectImageEntity;
import avvillas.template_admin.persistence.entity.ObjectTableEntity;
import avvillas.template_admin.service.dto.object.request.CellDTO;
import avvillas.template_admin.service.dto.object.request.ObjectGenericDTO;
import avvillas.template_admin.service.dto.object.request.ObjectImageDTO;
import avvillas.template_admin.service.dto.object.request.ObjectTableDTO;
import avvillas.template_admin.service.dto.object.response.CellResponseDTO;
import avvillas.template_admin.service.dto.object.response.ObjectGenericResponseDTO;
import avvillas.template_admin.service.dto.object.response.ObjectImageResponseDTO;
import avvillas.template_admin.service.dto.object.response.ObjectResponseDTO;
import avvillas.template_admin.service.dto.object.response.ObjectTableResponseDTO;
import avvillas.template_admin.service.implementations.ObjectServiceImp.helpers.HeaderConverter;
import avvillas.template_admin.service.implementations.ObjectServiceImp.helpers.StylePropertiesConverter;
import avvillas.template_admin.util.ConversionUtil;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.Base64;
import java.util.List;

@Mapper(componentModel = "spring",
        imports = {ConversionUtil.class, Base64.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {HeaderConverter.class, StylePropertiesConverter.class})
public interface ObjectMapper {

    @Mapping(target = "styleProperties", source = "style")
    @Mapping(target = "esCierreTabla", source = "esCierreTabla")
    @Mapping(target = "header", source = "header")
    @Mapping(target = "fixed", expression = "java(ConversionUtil.booleanToInteger(dto.getFixed()))")
    @Mapping(target = "body", source = "body")
    @Mapping(target = "cells", ignore = true)
    ObjectTableEntity toTablaEntity(ObjectTableDTO dto);

    @Mapping(target = "styleProperties", source = "style")
    @Mapping(target = "esCierreTabla", source = "esCierreTabla")
    ObjectEntity genericToEntity(ObjectGenericDTO dto);

    @Mapping(target = "image", source = "dto.decodedImage")
    @Mapping(target = "mimeType", source = "dto.mimeType")
    @Mapping(target = "styleProperties", source = "style")
    @Mapping(target = "esCierreTabla", source = "esCierreTabla")
    ObjectImageEntity imageToEntity(ObjectImageDTO dto);

    @Mapping(target = "fixed", ignore = true)
    @Mapping(target = "tableObject", ignore = true)
    @Mapping(target = "inputStructureProductId", ignore = true)
    @Mapping(target = "rowIndex", source = "rowIndex")
    @Mapping(target = "columnIndex", source = "columnIndex")
    @Mapping(target = "styleProperties", source = "style")
    @Mapping(target = "content", source = "content")
    CellEntity toCellEntity(CellDTO dto);

    @Named("toResponseDTO")
    default ObjectResponseDTO toResponseDTO(ObjectEntity entity) {
        if (entity instanceof ObjectTableEntity) {
            return toTableResponseDTO((ObjectTableEntity) entity);
        } else if (entity instanceof ObjectImageEntity) {
            return toImageResponseDTO((ObjectImageEntity) entity);
        } else {
            return toGenericResponseDTO(entity);
        }
    }

    @IterableMapping(qualifiedByName = "toResponseDTO")
    List<ObjectResponseDTO> toResponseListDTO(List<ObjectEntity> entities);

    @Mapping(target = "header", source = "header")
    @Mapping(target = "fixed", expression = "java(ConversionUtil.integerToBoolean(entity.getFixed()))")
    @Mapping(target = "body.globalStyles", source = "body.globalStyles")
    @Mapping(target = "body.cells", source = "cells")
    ObjectTableResponseDTO toTableResponseDTO(ObjectTableEntity entity);

    @Mapping(target = "content", source = "content")
    @Mapping(target = "style", source = "styleProperties")
    ObjectGenericResponseDTO toGenericResponseDTO(ObjectEntity entity);

    @Mapping(target = "image", expression = "java(mapImageToBase64(entity.getImage(), entity.getMimeType()))")
    @Mapping(target = "style", source = "styleProperties")
    ObjectImageResponseDTO toImageResponseDTO(ObjectImageEntity entity);

    @Mapping(target = "rowIndex", source = "rowIndex")
    @Mapping(target = "columnIndex", source = "columnIndex")
    @Mapping(target = "style", source = "styleProperties")
    @Mapping(target = "content", source = "content")
    @Mapping(target = "colSpan", source = "colSpan")
    @Mapping(target = "rowSpan", source = "rowSpan")
    @Mapping(target = "id", source = "id")
    CellResponseDTO toCellResponseDTO(CellEntity cell);

    List<CellResponseDTO> toCeldaResponseDTOList(List<CellEntity> cells);

    default String mapImageToBase64(byte[] image, String mimeType) {
        return image != null ?
                "data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(image) :
                null;
    }
}
