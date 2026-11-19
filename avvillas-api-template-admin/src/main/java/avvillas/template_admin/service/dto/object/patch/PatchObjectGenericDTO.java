package avvillas.template_admin.service.dto.object.patch;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Optional;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PatchObjectGenericDTO extends PatchObjectBaseDTO {
    private Optional<String> content = Optional.empty();
}
