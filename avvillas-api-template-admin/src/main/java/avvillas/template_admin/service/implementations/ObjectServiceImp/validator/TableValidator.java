package avvillas.template_admin.service.implementations.ObjectServiceImp.validator;

import avvillas.template_admin.constant.MessageConstant;
import avvillas.template_admin.constant.message.ObjectMessage;
import avvillas.template_admin.persistence.entity.ObjectTableEntity;
import avvillas.template_admin.service.dto.object.TableBodyConfig;
import avvillas.template_admin.service.dto.object.TableHeaderConfig;
import avvillas.template_admin.service.dto.object.patch.PatchObjectBaseDTO;
import avvillas.template_admin.service.dto.object.patch.PatchObjectTableDTO;
import avvillas.template_admin.service.dto.object.request.ObjectTableDTO;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
public class TableValidator {

    public void validateCreation(ObjectTableDTO dto) {
        if (dto.getBody().getCells() != null && !dto.getFixed()) {
            throw new IllegalArgumentException(ObjectMessage.TABLE_FIXED_CELLS_INVALID);
        }
        validateHeader(dto.getHeader());
        validateBody(dto.getBody());
    }

    public void validateHeader(TableHeaderConfig header) {
        if (header.getColumns().isEmpty()) {
            throw new DataIntegrityViolationException(ObjectMessage.HEADER_MIN);
        }
    }

    public void validateBody(TableBodyConfig body) {
        if (body == null) {
            throw new DataIntegrityViolationException(ObjectMessage.BODY_REQUIRED);
        }
    }

    public void validateTypeChange(PatchObjectBaseDTO dto) {

        if (!(dto instanceof PatchObjectTableDTO tableDto)) {
            throw new IllegalArgumentException(MessageConstant.format(ObjectMessage.CHANGE_TYPE_REQUIRED_FULL, ObjectMessage.TABLE_TYPE));
        }

        if (tableDto.getHeader().isEmpty()) {
            throw new IllegalArgumentException(ObjectMessage.HEADER_REQUIRED);
        }

        TableHeaderConfig header = tableDto.getHeader().get();
        if (header.getColumns() == null || header.getColumns().isEmpty()) {
            throw new IllegalArgumentException(ObjectMessage.HEADER_MIN);
        }

        if (tableDto.getBody().isEmpty()) {
            throw new IllegalArgumentException(ObjectMessage.BODY_REQUIRED);
        }

        if (tableDto.getBody().get().getCells() != null && !tableDto.getBody().get().getCells().isEmpty() &&
                (tableDto.getFixed().isEmpty() || !tableDto.getFixed().get())) {
            throw new IllegalArgumentException(ObjectMessage.TABLE_FIXED_CELLS_INVALID);
        }
    }

    public void validateUpdate(ObjectTableEntity existingEntity, PatchObjectTableDTO dto) {

        if (dto.getHeader().isPresent()) {
            validateHeader(dto.getHeader().get());
        }

        if (dto.getBody().isPresent()) {
            validateBody(dto.getBody().get());

            if (dto.getBody().get().getCells() != null && !dto.getBody().get().getCells().isEmpty() && existingEntity.getFixed().equals(0)) {
                throw new IllegalArgumentException(ObjectMessage.TABLE_FIXED_CELLS_INVALID);
            }
        }

        if (dto.getFixed().isPresent() && !dto.getFixed().get()) {
            boolean hasCells = existingEntity.getCells() != null && !existingEntity.getCells().isEmpty();
            if (hasCells) {
                throw new IllegalArgumentException(ObjectMessage.TABLE_FIXED_CELLS_INVALID);
            }
        }
    }
}
