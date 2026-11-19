package avvillas.template_admin.constant.fields;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ObjectFieldNames {
    IDENTIFIER("identifier"),
    NAME("name"),
    PRODUCT_ID("productId");

    private final String fieldName;
}
