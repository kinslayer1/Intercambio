package avvillas.template_admin.constant.fields;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FormatFieldNames {
    VERSION("version"),
    ID("id");

    private final String fieldName;
}
