package avvillas.template_admin.service.dto.object.patch;

import avvillas.template_admin.service.dto.object.TableBodyConfig;
import avvillas.template_admin.service.dto.object.TableHeaderConfig;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Optional;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PatchObjectTableDTO extends PatchObjectBaseDTO {
    @Valid
    private Optional<@Valid TableHeaderConfig> header = Optional.empty();

    private Optional<Boolean> fixed = Optional.empty();
    
    @Valid
    private Optional<@Valid TableBodyConfig> body = Optional.empty();
}
