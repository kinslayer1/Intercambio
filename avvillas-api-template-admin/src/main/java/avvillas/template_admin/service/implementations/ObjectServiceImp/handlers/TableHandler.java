package avvillas.template_admin.service.implementations.ObjectServiceImp.handlers;

import avvillas.template_admin.constant.MessageConstant;
import avvillas.template_admin.constant.message.ObjectMessage;
import avvillas.template_admin.persistence.entity.CellEntity;
import avvillas.template_admin.persistence.entity.ObjectEntity;
import avvillas.template_admin.persistence.entity.ObjectTableEntity;
import avvillas.template_admin.persistence.mapper.ObjectMapper;
import avvillas.template_admin.persistence.repository.CellRepository;
import avvillas.template_admin.persistence.repository.ObjectTableRepository;
import avvillas.template_admin.service.dto.object.patch.PatchObjectBaseDTO;
import avvillas.template_admin.service.dto.object.patch.PatchObjectTableDTO;
import avvillas.template_admin.service.dto.object.request.CellDTO;
import avvillas.template_admin.service.dto.object.request.ObjectBaseDTO;
import avvillas.template_admin.service.dto.object.request.ObjectTableDTO;
import avvillas.template_admin.service.dto.object.response.ObjectResponseDTO;
import avvillas.template_admin.service.implementations.ObjectServiceImp.helpers.BasePropertiesUpdater;
import avvillas.template_admin.service.implementations.ObjectServiceImp.helpers.CellUpdater;
import avvillas.template_admin.service.implementations.ObjectServiceImp.validator.ObjectValidator;
import avvillas.template_admin.service.implementations.ObjectServiceImp.validator.TableValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
class TableHandler implements ObjectHandler {

    private final ObjectTableRepository tableRepository;
    private final CellRepository cellRepository;
    private final ObjectMapper mapper;
    private final TableValidator validator;
    private final ObjectValidator objectValidator;
    private final BasePropertiesUpdater propertiesUpdater;

    @Override
    @Transactional
    public ObjectResponseDTO create(ObjectBaseDTO dto) {
        if (!(dto instanceof ObjectTableDTO tableDTO)) {
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
        validator.validateCreation(tableDTO);

        ObjectTableEntity entity = mapper.toTablaEntity(tableDTO);
        if (!Boolean.TRUE.equals(entity.getRenderCondicionalHabilitado())) {
            entity.setRenderCondicionalHabilitado(false);
            entity.setRenderCondicionalCampoId(null);
            entity.setRenderCondicionalValorEsperado(null);
            entity.setRenderCondicionalOperador(ObjectMessage.CONDITIONAL_RENDER_OPERATOR_EQUALS);
        }
        tableRepository.save(entity);

        if (tableDTO.getFixed() && tableDTO.getBody().getCells() != null) {
            createCells(entity, tableDTO.getBody().getCells());
        }

        return mapper.toTableResponseDTO(entity);
    }

    private void createCells(ObjectTableEntity table, List<CellDTO> cells) {
        List<CellEntity> entities = cells.stream()
                .map(cellDto -> {
                    CellEntity cell = mapper.toCellEntity(cellDto);
                    cell.setTableObject(table);
                    cell.setFixed(1);
                    return cell;
                }).toList();

        cellRepository.saveAll(entities);
        table.setCells(entities);
    }

    @Override
    @Transactional
    public ObjectResponseDTO update(ObjectEntity entity, PatchObjectBaseDTO dto) {
        ObjectTableEntity tableEntity = (ObjectTableEntity) entity;
        PatchObjectTableDTO tableDto = (PatchObjectTableDTO) dto;
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

        propertiesUpdater.update(tableEntity, dto);
        if (tableEntity.getRenderCondicionalHabilitado() == null) {
            tableEntity.setRenderCondicionalHabilitado(false);
        }
        if (tableEntity.getRenderCondicionalOperador() == null || tableEntity.getRenderCondicionalOperador().isBlank()) {
            tableEntity.setRenderCondicionalOperador(ObjectMessage.CONDITIONAL_RENDER_OPERATOR_EQUALS);
        }

        validator.validateUpdate(tableEntity, tableDto);
        updateTableSpecificProperties(tableEntity, tableDto);

        return mapper.toTableResponseDTO(tableRepository.save(tableEntity));
    }

    private void updateTableSpecificProperties(ObjectTableEntity entity, PatchObjectTableDTO dto) {
        dto.getFixed().ifPresent(bool ->
                entity.setFixed(bool ? 1 : 0)
        );
        dto.getHeader().ifPresent(entity::setHeader);
        dto.getBody().ifPresent(body ->
                updateCells(entity, body.getCells())
        );
    }

    private void updateCells(ObjectTableEntity table, List<CellDTO> cells) {
        CellUpdater.updateCells(table, cells, cellRepository, mapper);
    }

    @Override
    @Transactional
    public void deleteWithDependencies(ObjectEntity entity) {
        ObjectTableEntity table = (ObjectTableEntity) entity;
        objectValidator.validateObjectExistInFormat(entity.getId());
        cellRepository.deleteAll(table.getCells());
        tableRepository.delete(table);
    }
}
