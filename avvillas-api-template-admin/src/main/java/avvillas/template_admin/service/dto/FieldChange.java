package avvillas.template_admin.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class FieldChange {
    public String fieldName;
    public Object previousValue;
    public Object newValue;
}
