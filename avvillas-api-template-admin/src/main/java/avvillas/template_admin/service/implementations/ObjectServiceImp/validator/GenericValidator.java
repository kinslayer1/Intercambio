package avvillas.template_admin.service.implementations.ObjectServiceImp.validator;

import avvillas.template_admin.constant.MessageConstant;
import avvillas.template_admin.constant.message.ObjectMessage;
import avvillas.template_admin.service.dto.object.patch.PatchObjectBaseDTO;
import avvillas.template_admin.service.dto.object.patch.PatchObjectGenericDTO;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class GenericValidator {

    public void validate(String type, String content) {
        if (!Objects.equals(type, ObjectMessage.SHAPE_TYPE) && (content == null || content.isBlank())) {
            throw new IllegalArgumentException(ObjectMessage.CONTENT_REQUIRED);
        }
    }

    public void validateTypeChange(PatchObjectBaseDTO dto) {

        if (!(dto instanceof PatchObjectGenericDTO genericDto)) {
            throw new IllegalArgumentException(MessageConstant.format(ObjectMessage.CHANGE_TYPE_REQUIRED_FULL, dto.getType()));
        }

        if (genericDto.getContent().isEmpty()) {
            throw new IllegalArgumentException(MessageConstant.format(ObjectMessage.CONTENT_REQUIRED_BY_TYPE, dto.getType()));
        }

    }
}