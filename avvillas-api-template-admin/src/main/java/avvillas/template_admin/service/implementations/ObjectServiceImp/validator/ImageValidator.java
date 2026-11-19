package avvillas.template_admin.service.implementations.ObjectServiceImp.validator;

import avvillas.template_admin.constant.MessageConstant;
import avvillas.template_admin.constant.message.ObjectMessage;
import avvillas.template_admin.service.dto.object.patch.PatchObjectBaseDTO;
import avvillas.template_admin.service.dto.object.patch.PatchObjectImageDTO;
import avvillas.template_admin.service.dto.object.request.ObjectImageDTO;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.List;

@Component
public class ImageValidator {

    private static final List<String> ALLOWED_MIME_TYPES = List.of("image/jpeg", "image/png");


    public void validateCreation(ObjectImageDTO dto) {
        validateImage(dto.getImage());
    }

    public void validateUpdate(PatchObjectImageDTO dto) {
        dto.getImage().ifPresent(this::validateImage);
    }

    private void validateImage(String base64Content) {
        if (base64Content == null || base64Content.isBlank()) {
            throw new IllegalArgumentException(ObjectMessage.IMAGE_REQUIRED);
        }

        if (base64Content.length() > ObjectMessage.MAX_BASE64_LENGTH) {
            throw new IllegalArgumentException(ObjectMessage.IMAGE_MAX_SIZE);
        }

        String[] parts = base64Content.split(",");
        if (parts.length != 2) {
            throw new IllegalArgumentException(ObjectMessage.IMAGE_NOT_SUPPORTED_FORMAT);
        }

        String mimeType = parts[0].replace("data:", "").split(";")[0];
        if (!ALLOWED_MIME_TYPES.contains(mimeType)) {
            throw new IllegalArgumentException(ObjectMessage.IMAGE_NOT_SUPPORTED_FORMAT);
        }

        byte[] decoded = decodeBase64(parts[1]);
        if (decoded.length > ObjectMessage.MAX_SIZE_BYTES) {
            throw new IllegalArgumentException(ObjectMessage.IMAGE_MAX_SIZE);
        }
    }

    private byte[] decodeBase64(String base64) {
        try {
            return Base64.getDecoder().decode(base64);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(ObjectMessage.IMAGE_NOT_SUPPORTED_FORMAT);
        }
    }

    public void validateTypeChange(PatchObjectBaseDTO dto) {

        if (!(dto instanceof PatchObjectImageDTO imageDto)) {
            throw new IllegalArgumentException(MessageConstant.format(ObjectMessage.CHANGE_TYPE_REQUIRED_FULL, dto.getType()));
        }

        if (imageDto.getImage().isEmpty()) {
            throw new IllegalArgumentException(MessageConstant.format(ObjectMessage.IMAGE_REQUIRED, dto.getType()));
        }

    }
}