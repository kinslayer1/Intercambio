package avvillas.template_admin.service.specification;

import org.springframework.data.jpa.domain.Specification;

public class SpecificationUtils {

    public static <T> Specification<T> likeIgnoreCase(String field, String value) {
        return (root, query, cb) -> {
            if (value == null || value.trim().isEmpty()) {
                return null;
            }
            String searchLower = value.toLowerCase().trim();
            return cb.like(cb.lower(root.get(field)), "%" + searchLower + "%");
        };
    }

    public static <T> Specification<T> likeIgnoreCaseNested(String parentField, String childField, String value) {
        return (root, query, cb) -> {
            if (value == null || value.trim().isEmpty()) {
                return null;
            }
            String searchLower = value.toLowerCase().trim();
            return cb.like(cb.lower(root.get(parentField).get(childField)), "%" + searchLower + "%");
        };
    }


    public static <T> Specification<T> equals(String field, Object value) {
        return (root, query, cb) -> value == null ? null : cb.equal(root.get(field), value);
    }

    public static <T> Specification<T> numericFilter(String field, String search) {
        return (root, query, cb) -> {
            if (isValidNumeric(search)) {
                return cb.equal(root.get(field), Integer.parseInt(search));
            }
            return null;
        };
    }

    private static boolean isValidNumeric(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
