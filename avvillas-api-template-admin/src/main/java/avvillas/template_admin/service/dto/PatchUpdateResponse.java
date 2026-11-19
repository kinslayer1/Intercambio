package avvillas.template_admin.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class PatchUpdateResponse<T> {
    private List<FieldChange> differences;
    private T updatedEntity;
}
