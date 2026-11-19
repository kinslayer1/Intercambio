package avvillas.template_admin.service.dto.object.request;

import avvillas.template_admin.constant.message.ObjectMessage;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Base64;

@Data
@EqualsAndHashCode(callSuper = true)
public class ObjectImageDTO extends ObjectBaseDTO {
    @NotBlank(message = ObjectMessage.IMAGE_REQUIRED)
    @Pattern(regexp = ObjectMessage.REGEX_BASE64_IMAGE,
            message = ObjectMessage.IMAGE_NOT_SUPPORTED_FORMAT)
    private String image;

    @JsonIgnore
    public byte[] getDecodedImage() {
        if (image == null) return new byte[0];
        return Base64.getDecoder().decode(image.split(",")[1]);
    }

    @JsonIgnore
    public String getMimeType() {
        return image != null ?
                image.split(";")[0].replace("data:", "") :
                null;
    }

}
