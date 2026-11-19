package avvillas.template_admin.service.implementations.ObjectServiceImp.handlers;

import avvillas.template_admin.constant.MessageConstant;
import avvillas.template_admin.constant.message.ObjectMessage;
import avvillas.template_admin.persistence.entity.ObjectEntity;
import avvillas.template_admin.persistence.entity.ObjectImageEntity;
import avvillas.template_admin.persistence.mapper.ObjectMapper;
import avvillas.template_admin.persistence.repository.ObjectImageRepository;
import avvillas.template_admin.service.dto.object.patch.PatchObjectBaseDTO;
import avvillas.template_admin.service.dto.object.patch.PatchObjectImageDTO;
import avvillas.template_admin.service.dto.object.request.ObjectBaseDTO;
import avvillas.template_admin.service.dto.object.request.ObjectImageDTO;
import avvillas.template_admin.service.dto.object.response.ObjectResponseDTO;
import avvillas.template_admin.service.implementations.ObjectServiceImp.helpers.BasePropertiesUpdater;
import avvillas.template_admin.service.implementations.ObjectServiceImp.validator.ImageValidator;
import avvillas.template_admin.service.implementations.ObjectServiceImp.validator.ObjectValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;

@Service
@RequiredArgsConstructor
class ImageHandler implements ObjectHandler {

    private final ObjectImageRepository objectImageRepository;
    private final ObjectMapper mapper;
    private final ImageValidator imageValidator;
    private final ObjectValidator objectValidator;
    private final BasePropertiesUpdater propertiesUpdater;

    @Override
    @Transactional
    public ObjectResponseDTO create(ObjectBaseDTO dto) {
        if (!(dto instanceof ObjectImageDTO imageDTO)) {
            throw new IllegalArgumentException(MessageConstant.format(ObjectMessage.VERIFY_FORMAT_DATA, dto.getType()));
        }
        objectValidator.validateProductExist(dto.getProductId());
        objectValidator.validateConditionalRenderConfiguration(
                dto.getProductId(),
                dto.getRenderCondicionalHabilitado(),
                dto.getRenderCondicionalCampoId(),
                dto.getRenderCondicionalOperador(),
                dto.getRenderCondicionalValorEsperado()
        );
        if (dto.getRenderCondicionalOperador() == null || dto.getRenderCondicionalOperador().isBlank()) {
            dto.setRenderCondicionalOperador(ObjectMessage.CONDITIONAL_RENDER_OPERATOR_EQUALS);
        }
        imageValidator.validateCreation(imageDTO);
        ObjectImageEntity entity = mapper.imageToEntity(imageDTO);
        if (!Boolean.TRUE.equals(entity.getRenderCondicionalHabilitado())) {
            entity.setRenderCondicionalHabilitado(false);
            entity.setRenderCondicionalCampoId(null);
            entity.setRenderCondicionalValorEsperado(null);
            entity.setRenderCondicionalOperador(ObjectMessage.CONDITIONAL_RENDER_OPERATOR_EQUALS);
        }
        return mapper.toImageResponseDTO(objectImageRepository.save(entity));
    }

    @Override
    @Transactional
    public ObjectResponseDTO update(ObjectEntity entity, PatchObjectBaseDTO dto) {
        ObjectImageEntity imageEntity = (ObjectImageEntity) entity;
        PatchObjectImageDTO imageDto = (PatchObjectImageDTO) dto;

        dto.getProductId().ifPresent(objectValidator::validateProductExist);

        String resolvedProductId = dto.getProductId().orElse(entity.getProductId());
        boolean resolvedEnabled = dto.getRenderCondicionalHabilitado().orElse(Boolean.TRUE.equals(entity.getRenderCondicionalHabilitado()));
        String resolvedFieldId = dto.getRenderCondicionalCampoId().orElse(entity.getRenderCondicionalCampoId());
        String resolvedOperator = dto.getRenderCondicionalOperador().orElse(entity.getRenderCondicionalOperador());
        String resolvedExpectedValue = dto.getRenderCondicionalValorEsperado().orElse(entity.getRenderCondicionalValorEsperado());

        objectValidator.validateConditionalRenderConfiguration(
                resolvedProductId,
                resolvedEnabled,
                resolvedFieldId,
                resolvedOperator,
                resolvedExpectedValue
        );

        imageValidator.validateUpdate(imageDto);

        propertiesUpdater.update(imageEntity, dto);
        if (imageEntity.getRenderCondicionalHabilitado() == null) {
            imageEntity.setRenderCondicionalHabilitado(false);
        }
        if (imageEntity.getRenderCondicionalOperador() == null || imageEntity.getRenderCondicionalOperador().isBlank()) {
            imageEntity.setRenderCondicionalOperador(ObjectMessage.CONDITIONAL_RENDER_OPERATOR_EQUALS);
        }

        imageDto.getImage().ifPresent(image ->
                updateImageData(imageEntity, image));

        return mapper.toImageResponseDTO(objectImageRepository.save(imageEntity));
    }

    private void updateImageData(ObjectImageEntity entity, String content) {
        String[] parts = content.split(",");
        entity.setMimeType(parts[0].replace("data:", "").split(";")[0]);
        entity.setImage(Base64.getDecoder().decode(parts[1]));
    }

    @Override
    public void deleteWithDependencies(ObjectEntity entity) {
        ObjectImageEntity image = (ObjectImageEntity) entity;
        objectValidator.validateObjectExistInFormat(entity.getId());
        objectImageRepository.delete(image);
    }
}
