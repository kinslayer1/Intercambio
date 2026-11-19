package avvillas.template_admin.service.implementations.ObjectServiceImp.helpers;

import avvillas.template_admin.constant.message.ObjectMessage;
import avvillas.template_admin.persistence.entity.ObjectEntity;
import avvillas.template_admin.service.dto.object.patch.PatchObjectBaseDTO;
import avvillas.template_admin.service.dto.object.patch.PatchObjectGenericDTO;
import avvillas.template_admin.service.dto.object.patch.PatchObjectImageDTO;
import avvillas.template_admin.service.dto.object.patch.PatchObjectTableDTO;
import avvillas.template_admin.service.dto.object.request.ObjectBaseDTO;
import avvillas.template_admin.service.dto.object.request.ObjectGenericDTO;
import avvillas.template_admin.service.dto.object.request.ObjectImageDTO;
import avvillas.template_admin.service.dto.object.request.ObjectTableDTO;
import org.springframework.stereotype.Component;

@Component
public class TypeConversionHandler {

    public static ObjectBaseDTO convertType(ObjectEntity oldEntity, PatchObjectBaseDTO dto) {
        return switch (dto.getType().get()) {
            case ObjectMessage.TABLE_TYPE -> convertToTable(oldEntity, (PatchObjectTableDTO) dto);
            case ObjectMessage.IMAGE_TYPE -> convertToImage(oldEntity, (PatchObjectImageDTO) dto);
            case ObjectMessage.TEXT_TYPE, ObjectMessage.SHAPE_TYPE ->
                    convertToGeneric(oldEntity, (PatchObjectGenericDTO) dto);
            default -> throw new UnsupportedOperationException(ObjectMessage.TYPE_NOT_SUPPORTED);
        };
    }

    private static ObjectTableDTO convertToTable(ObjectEntity entity, PatchObjectTableDTO dto) {
        ObjectTableDTO tableDTO = new ObjectTableDTO();
        tableDTO.setProductId(entity.getProductId());
        tableDTO.setName(dto.getName().get());
        tableDTO.setIdentifier(dto.getIdentifier().get());
        tableDTO.setType(dto.getType().get());
        tableDTO.setStyle(dto.getStyle().orElse(null));
        tableDTO.setEsCierreTabla(dto.getEsCierreTabla().orElse(entity.getEsCierreTabla()));
        tableDTO.setRenderCondicionalHabilitado(dto.getRenderCondicionalHabilitado().orElse(Boolean.TRUE.equals(entity.getRenderCondicionalHabilitado())));
        tableDTO.setRenderCondicionalCampoId(dto.getRenderCondicionalCampoId().orElse(entity.getRenderCondicionalCampoId()));
        tableDTO.setRenderCondicionalOperador(dto.getRenderCondicionalOperador().orElse(entity.getRenderCondicionalOperador()));
        tableDTO.setRenderCondicionalValorEsperado(dto.getRenderCondicionalValorEsperado().orElse(entity.getRenderCondicionalValorEsperado()));
        tableDTO.setHeader(dto.getHeader().get());
        tableDTO.setBody(dto.getBody().get());
        tableDTO.setFixed(dto.getFixed().orElse(false));
        return tableDTO;
    }

    private static ObjectGenericDTO convertToGeneric(ObjectEntity entity, PatchObjectGenericDTO dto) {
        ObjectGenericDTO genericDTO = new ObjectGenericDTO();
        genericDTO.setProductId(entity.getProductId());
        genericDTO.setName(dto.getName().get());
        genericDTO.setIdentifier(dto.getIdentifier().get());
        genericDTO.setType(dto.getType().get());
        genericDTO.setStyle(dto.getStyle().orElse(null));
        genericDTO.setEsCierreTabla(dto.getEsCierreTabla().orElse(entity.getEsCierreTabla()));
        genericDTO.setRenderCondicionalHabilitado(dto.getRenderCondicionalHabilitado().orElse(Boolean.TRUE.equals(entity.getRenderCondicionalHabilitado())));
        genericDTO.setRenderCondicionalCampoId(dto.getRenderCondicionalCampoId().orElse(entity.getRenderCondicionalCampoId()));
        genericDTO.setRenderCondicionalOperador(dto.getRenderCondicionalOperador().orElse(entity.getRenderCondicionalOperador()));
        genericDTO.setRenderCondicionalValorEsperado(dto.getRenderCondicionalValorEsperado().orElse(entity.getRenderCondicionalValorEsperado()));
        genericDTO.setContent(dto.getContent().get());
        return genericDTO;
    }

    private static ObjectImageDTO convertToImage(ObjectEntity entity, PatchObjectImageDTO dto) {
        ObjectImageDTO genericDTO = new ObjectImageDTO();
        genericDTO.setProductId(entity.getProductId());
        genericDTO.setName(dto.getName().get());
        genericDTO.setIdentifier(dto.getIdentifier().get());
        genericDTO.setType(dto.getType().get());
        genericDTO.setStyle(dto.getStyle().orElse(null));
        genericDTO.setEsCierreTabla(dto.getEsCierreTabla().orElse(entity.getEsCierreTabla()));
        genericDTO.setRenderCondicionalHabilitado(dto.getRenderCondicionalHabilitado().orElse(Boolean.TRUE.equals(entity.getRenderCondicionalHabilitado())));
        genericDTO.setRenderCondicionalCampoId(dto.getRenderCondicionalCampoId().orElse(entity.getRenderCondicionalCampoId()));
        genericDTO.setRenderCondicionalOperador(dto.getRenderCondicionalOperador().orElse(entity.getRenderCondicionalOperador()));
        genericDTO.setRenderCondicionalValorEsperado(dto.getRenderCondicionalValorEsperado().orElse(entity.getRenderCondicionalValorEsperado()));
        genericDTO.setImage(dto.getImage().get());
        return genericDTO;
    }
}
