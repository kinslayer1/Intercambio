package avvillas.template_admin.service.specification;

import avvillas.template_admin.constant.fields.FormatFieldNames;
import avvillas.template_admin.constant.fields.ObjectFieldNames;
import avvillas.template_admin.constant.fields.common.CommonFieldNames;
import avvillas.template_admin.constant.fields.common.CommonState;
import avvillas.template_admin.persistence.entity.FormatEntity;

import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class FormatSpecification {

    public static Specification<FormatEntity> filterByProductIds(List<String> productIds) {
        return (root, query, criteriaBuilder) -> {
            if (productIds == null || productIds.isEmpty()) {
                return criteriaBuilder.disjunction();
            }
            return root.get(ObjectFieldNames.PRODUCT_ID.getFieldName()).in(productIds);
        };
    }

    public static Specification<FormatEntity> filterByVersion(String version) {
        return SpecificationUtils.numericFilter(FormatFieldNames.VERSION.getFieldName(), version);
    }

    public static Specification<FormatEntity> filterById(String id) {
        return SpecificationUtils.likeIgnoreCase(FormatFieldNames.ID.getFieldName(), id);
    }

    public static Specification<FormatEntity> filterByState(String search) {
        return (root, query, cb) -> {
            if (search == null || search.trim().isEmpty()) {
                return null;
            }
            String searchLower = search.toLowerCase().trim();
            if (CommonFieldNames.ACTIVE_STATE.getFieldName().equals(searchLower)) {
                return cb.equal(root.get(CommonFieldNames.ACTIVE.getFieldName()), CommonState.ACTIVE.getValue());
            } else if (CommonFieldNames.INACTIVE_STATE.getFieldName().equals(searchLower)) {
                return cb.equal(root.get(CommonFieldNames.ACTIVE.getFieldName()), CommonState.INACTIVE.getValue());
            }
            return null;
        };
    }
}
