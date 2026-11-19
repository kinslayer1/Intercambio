package avvillas.template_admin.constant.fields.common;

import lombok.Getter;

@Getter
public enum CommonState {
    ACTIVE(1),
    INACTIVE(0),
    BLOCKED(3);

    private final int value;

    CommonState(int value) {
        this.value = value;
    }

}
