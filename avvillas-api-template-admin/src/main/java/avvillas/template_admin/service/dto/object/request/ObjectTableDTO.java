package avvillas.template_admin.service.dto.object.request;

import avvillas.template_admin.constant.message.ObjectMessage;
import avvillas.template_admin.service.dto.object.TableBodyConfig;
import avvillas.template_admin.service.dto.object.TableHeaderConfig;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ObjectTableDTO extends ObjectBaseDTO {
    @NotNull(message = ObjectMessage.HEADER_REQUIRED)
    @Valid
    private TableHeaderConfig header;

    @NotNull(message = ObjectMessage.FIXED_REQUIRED)
    private Boolean fixed;

    @NotNull(message = ObjectMessage.BODY_REQUIRED)
    @Valid
    private TableBodyConfig body;
}
