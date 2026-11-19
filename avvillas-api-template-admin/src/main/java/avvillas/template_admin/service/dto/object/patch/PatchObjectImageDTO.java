// PatchObjectImageDTO.java
package avvillas.template_admin.service.dto.object.patch;

import avvillas.template_admin.constant.message.ObjectMessage;
import avvillas.template_admin.validation.annotation.OptionalPattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Optional;

@Data
@EqualsAndHashCode(callSuper = true)
public class PatchObjectImageDTO extends PatchObjectBaseDTO {

    @OptionalPattern(
            regexp = ObjectMessage.REGEX_BASE64_IMAGE,
            message = ObjectMessage.IMAGE_NOT_SUPPORTED_FORMAT
    )
    private Optional<String> image = Optional.empty();
}