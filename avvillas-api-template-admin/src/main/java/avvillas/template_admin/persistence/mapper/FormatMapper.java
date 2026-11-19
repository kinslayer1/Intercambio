package avvillas.template_admin.persistence.mapper;

import avvillas.template_admin.persistence.entity.FormatEntity;
import avvillas.template_admin.persistence.entity.FormatObjectsEntity;
import avvillas.template_admin.persistence.entity.FormatPageEntity;
import avvillas.template_admin.persistence.entity.ObjectEntity;
import avvillas.template_admin.service.dto.format.FormatDto;
import avvillas.template_admin.util.ConversionUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring", imports = {ConversionUtil.class})
public interface FormatMapper {

    @Mapping(target = "active", expression = "java(ConversionUtil.booleanToInteger(dto.getActive()))")
    @Mapping(target = "topMargin", source = "dto.pdfConfig.margins.top")
    @Mapping(target = "bottomMargin", source = "dto.pdfConfig.margins.bottom")
    @Mapping(target = "leftMargin", source = "dto.pdfConfig.margins.left")
    @Mapping(target = "rightMargin", source = "dto.pdfConfig.margins.right")
    @Mapping(target = "paperType", source = "dto.pdfConfig.paperType")
    @Mapping(target = "continues", source = "dto.pdfConfig.continues")
    @Mapping(target = "fontFamily", source = "dto.pdfConfig.fontFamily")
    @Mapping(target = "pages", source = "dto.pages")
    FormatEntity toEntity(FormatDto dto);

    @Mapping(target = "format", ignore = true)
    @Mapping(target = "objects", expression = "java(combineElementsAndFields(pageDto.getElements(), pageDto.getFields()))")
    FormatPageEntity toPageEntity(FormatDto.PageDto pageDto);

    default List<FormatObjectsEntity> combineElementsAndFields(
            List<FormatDto.ElementDto> elements, List<FormatDto.FieldDto> fields) {
        List<FormatObjectsEntity> combined = new ArrayList<>();
        combined.addAll(toEntitiesFromElements(elements));
        combined.addAll(toEntitiesFromFields(fields));
        return combined;
    }

    List<FormatObjectsEntity> toEntitiesFromElements(List<FormatDto.ElementDto> elements);

    List<FormatObjectsEntity> toEntitiesFromFields(List<FormatDto.FieldDto> fields);

    @Mapping(target = "object", expression = "java(mapToObjectEntity(element.getObjectId()))")
    @Mapping(target = "fieldId", ignore = true)
    @Mapping(target = "page", ignore = true)
    FormatObjectsEntity toEntityFromElement(FormatDto.ElementDto element);

    @Mapping(target = "object", ignore = true)
    @Mapping(target = "fieldId", source = "fieldId")
    @Mapping(target = "page", ignore = true)
    FormatObjectsEntity toEntityFromField(FormatDto.FieldDto field);

    default ObjectEntity mapToObjectEntity(String objectId) {
        if (objectId == null) {
            return null;
        }
        ObjectEntity objectEntity = new ObjectEntity();
        objectEntity.setId(objectId);
        return objectEntity;
    }
}
