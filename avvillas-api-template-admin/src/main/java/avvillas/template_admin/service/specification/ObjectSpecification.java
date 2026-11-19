package avvillas.template_admin.service.specification;


import avvillas.template_admin.constant.fields.ObjectFieldNames;
import avvillas.template_admin.persistence.entity.ObjectEntity;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class ObjectSpecification {

    public static Specification<ObjectEntity> filterByIdentifier(String search) {
        return SpecificationUtils.likeIgnoreCase(ObjectFieldNames.IDENTIFIER.getFieldName(), search);
    }

    public static Specification<ObjectEntity> filterByName(String search) {
        return SpecificationUtils.likeIgnoreCase(ObjectFieldNames.NAME.getFieldName(), search);
    }

    public static Specification<ObjectEntity> filterByProductIds(List<String> productIds) {
        return (root, query, criteriaBuilder) -> {
            if (productIds == null || productIds.isEmpty()) {
                return criteriaBuilder.disjunction();
            }
            return root.get(ObjectFieldNames.PRODUCT_ID.getFieldName()).in(productIds);
        };
    }

}
