package avvillas.template_admin.constant.fields.common;

import lombok.Getter;

@Getter
public enum CommonFieldNames {

    ACTIVE("active"),

    ACTIVE_STATE("activo"),
    INACTIVE_STATE("inactivo"),
    BLOCKED_STATE("bloqueado");

    private final String fieldName;

    CommonFieldNames(String fieldName) {
        this.fieldName = fieldName;
    }

}
