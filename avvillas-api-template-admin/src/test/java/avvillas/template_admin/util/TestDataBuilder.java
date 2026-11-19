package avvillas.template_admin.util;

import avvillas.template_admin.service.dto.product.ProductDTO;

public final class TestDataBuilder {

    public static final String VALID_PRODUCT_ID = "prod-uuid-1234";

    private TestDataBuilder() {
    }

    public static ProductDTO createMockProductDto() {
        ProductDTO product = new ProductDTO();
        product.setId(VALID_PRODUCT_ID);
        product.setCode("PRD001");
        product.setDescription("Credit Card");
        product.setActive(true);
        return product;
    }

}
