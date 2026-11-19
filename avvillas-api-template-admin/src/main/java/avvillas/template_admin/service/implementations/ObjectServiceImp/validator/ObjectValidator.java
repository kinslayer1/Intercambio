package avvillas.template_admin.service.implementations.ObjectServiceImp.validator;

import avvillas.template_admin.constant.MessageConstant;
import avvillas.template_admin.constant.message.ObjectMessage;
import avvillas.template_admin.constant.message.ProductMessage;
import avvillas.template_admin.persistence.repository.FormatObjectsRepository;
import avvillas.template_admin.service.dto.object.patch.PatchObjectBaseDTO;
import avvillas.template_admin.service.dto.input_product_structure.InputProductStructureDto;
import avvillas.template_admin.web.traits.ProductTrait;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.NoSuchElementException;

@Component
@RequiredArgsConstructor
public class ObjectValidator {
    private final TableValidator tableValidator;
    private final ImageValidator imageValidator;
    private final GenericValidator genericValidator;
    private final ProductTrait productTrait;
    private final FormatObjectsRepository formatObjectsRepository;

    public void validateTypeChange(PatchObjectBaseDTO dto) {
        String newType = dto.getType().get();
        validateCommonMandatoryFields(dto);
        switch (newType) {
            case ObjectMessage.TABLE_TYPE -> tableValidator.validateTypeChange(dto);
            case ObjectMessage.IMAGE_TYPE -> imageValidator.validateTypeChange(dto);
            case ObjectMessage.TEXT_TYPE, ObjectMessage.SHAPE_TYPE -> genericValidator.validateTypeChange(dto);
            default ->
                    throw new UnsupportedOperationException(MessageConstant.format(ObjectMessage.TYPE_NOT_SUPPORTED, newType));
        }
    }

    private void validateCommonMandatoryFields(PatchObjectBaseDTO dto) {
        if (dto.getName().isEmpty() || dto.getIdentifier().isEmpty()) {
            throw new IllegalArgumentException(ObjectMessage.NAME_IDENTIFIER_REQUIRED);
        }
    }

    public void validateProductExist(String idProduct) {
        try {
            productTrait.getProductById(idProduct);
        } catch (Exception e) {
            throw new NoSuchElementException(MessageConstant.format(ProductMessage.PRODUCT_NOT_FOUND, idProduct));
        }
    }

    public void validateObjectExistInFormat(String objectId) {
        if (formatObjectsRepository.existsByObjectId(objectId)) {
            throw new IllegalArgumentException(ObjectMessage.OBJECT_EXIST_IN_FORMAT);
        }
    }

    public void validateConditionalRenderConfiguration(String productId,
                                                        Boolean enabled,
                                                        String fieldId,
                                                        String operator,
                                                        String expectedValue) {
        if (enabled == null || !enabled) {
            return;
        }

        if (fieldId == null || fieldId.isBlank()) {
            throw new IllegalArgumentException(ObjectMessage.CONDITIONAL_RENDER_FIELD_REQUIRED);
        }

        if (expectedValue == null || expectedValue.isBlank()) {
            throw new IllegalArgumentException(ObjectMessage.CONDITIONAL_RENDER_VALUE_REQUIRED);
        }

        if (operator != null && !operator.isBlank() && !ObjectMessage.CONDITIONAL_RENDER_OPERATOR_EQUALS.equalsIgnoreCase(operator)) {
            throw new IllegalArgumentException(ObjectMessage.CONDITIONAL_RENDER_OPERATOR_INVALID);
        }

        try {
            List<InputProductStructureDto> fields = productTrait.getInputProductStructureByIds(List.of(fieldId));
            if (fields.isEmpty()) {
                throw new IllegalArgumentException(ObjectMessage.CONDITIONAL_RENDER_FIELD_PRODUCT_INVALID);
            }

            InputProductStructureDto selectedField = fields.getFirst();
            if (!productId.equals(selectedField.getProductId())) {
                throw new IllegalArgumentException(ObjectMessage.CONDITIONAL_RENDER_FIELD_PRODUCT_INVALID);
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException(ObjectMessage.CONDITIONAL_RENDER_FIELD_PRODUCT_INVALID);
        }
    }
}
