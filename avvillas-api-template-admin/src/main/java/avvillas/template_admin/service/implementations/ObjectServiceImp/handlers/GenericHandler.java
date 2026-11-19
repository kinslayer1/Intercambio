package avvillas.template_admin.service.implementations.ObjectServiceImp.handlers;

import avvillas.template_admin.constant.MessageConstant;
import avvillas.template_admin.constant.message.ObjectMessage;
import avvillas.template_admin.persistence.entity.ObjectEntity;
import avvillas.template_admin.persistence.mapper.ObjectMapper;
import avvillas.template_admin.persistence.repository.ObjectRepository;
import avvillas.template_admin.service.dto.object.patch.PatchObjectBaseDTO;
import avvillas.template_admin.service.dto.object.patch.PatchObjectGenericDTO;
import avvillas.template_admin.service.dto.object.request.ObjectBaseDTO;
import avvillas.template_admin.service.dto.object.request.ObjectGenericDTO;
import avvillas.template_admin.service.dto.object.response.ObjectResponseDTO;
import avvillas.template_admin.service.implementations.ObjectServiceImp.helpers.BasePropertiesUpdater;
import avvillas.template_admin.service.implementations.ObjectServiceImp.validator.GenericValidator;
import avvillas.template_admin.service.implementations.ObjectServiceImp.validator.ObjectValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class GenericHandler implements ObjectHandler {

    private final ObjectRepository objectRepository;
    private final ObjectMapper mapper;
    private final GenericValidator validator;
    private final ObjectValidator objectValidator;
    private final BasePropertiesUpdater propertiesUpdater;

    @Override
    @Transactional
    public ObjectResponseDTO create(ObjectBaseDTO dto) {
        if (!(dto instanceof ObjectGenericDTO genericDto)) {
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
        validator.validate(genericDto.getType(), genericDto.getContent());
        if (dto.getRenderCondicionalOperador() == null || dto.getRenderCondicionalOperador().isBlank()) {
            dto.setRenderCondicionalOperador(ObjectMessage.CONDITIONAL_RENDER_OPERATOR_EQUALS);
        }
        ObjectEntity entity = mapper.genericToEntity(genericDto);
        if (!Boolean.TRUE.equals(entity.getRenderCondicionalHabilitado())) {
            entity.setRenderCondicionalHabilitado(false);
            entity.setRenderCondicionalCampoId(null);
            entity.setRenderCondicionalValorEsperado(null);
            entity.setRenderCondicionalOperador(ObjectMessage.CONDITIONAL_RENDER_OPERATOR_EQUALS);
        }
        return mapper.toResponseDTO(objectRepository.save(entity));
    }

    @Override
    @Transactional
    public ObjectResponseDTO update(ObjectEntity entity, PatchObjectBaseDTO dto) {
        PatchObjectGenericDTO genericDto = (PatchObjectGenericDTO) dto;
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
        propertiesUpdater.update(entity, dto);
        if (entity.getRenderCondicionalHabilitado() == null) {
            entity.setRenderCondicionalHabilitado(false);
        }
        if (entity.getRenderCondicionalOperador() == null || entity.getRenderCondicionalOperador().isBlank()) {
            entity.setRenderCondicionalOperador(ObjectMessage.CONDITIONAL_RENDER_OPERATOR_EQUALS);
        }
        genericDto.getContent().ifPresent(content -> {
            validator.validate(entity.getType(), content);
            entity.setContent(content);
        });
        return mapper.toResponseDTO(objectRepository.save(entity));
    }

    @Override
    public void deleteWithDependencies(ObjectEntity entity) {
        objectValidator.validateObjectExistInFormat(entity.getId());
        objectRepository.delete(entity);
    }
}
