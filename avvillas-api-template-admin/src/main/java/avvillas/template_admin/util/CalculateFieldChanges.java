package avvillas.template_admin.util;

import avvillas.template_admin.constant.fields.common.CommonFieldNames;
import avvillas.template_admin.service.dto.FieldChange;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class CalculateFieldChanges {

    private CalculateFieldChanges() {}

    public static List<FieldChange> calculate(Object sourceObject, Object targetObject) {
        Map<String, Field> targetFields = getAccessibleFields(targetObject);
        return Arrays.stream(sourceObject.getClass().getDeclaredFields())
                .peek(field -> field.setAccessible(true))
                .map(sourceField -> compareFields(sourceField, sourceObject, targetObject, targetFields))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private static Map<String, Field> getAccessibleFields(Object obj) {
        return Arrays.stream(obj.getClass().getDeclaredFields())
                .peek(field -> field.setAccessible(true))
                .collect(Collectors.toMap(Field::getName, field -> field));
    }

    private static FieldChange compareFields(Field sourceField, Object sourceObject, Object targetObject, Map<String, Field> targetFields) {
        try {
            String fieldName = sourceField.getName();
            Object sourceValue = sourceField.get(sourceObject);

            if (targetFields.containsKey(fieldName)) {
                Field targetField = targetFields.get(fieldName);
                Object targetValue = getTargetValue(targetField, targetObject);

                if (CommonFieldNames.ACTIVE.getFieldName().equals(fieldName))
                    return compareActiveField(sourceValue, targetValue, fieldName);

                if (valuesAreDifferent(sourceValue, targetValue))
                    return new FieldChange(fieldName, sourceValue, targetValue);
            }
        } catch (IllegalAccessException e) {
            return new FieldChange("", "", "");
        }
        return null;
    }

    private static Object getTargetValue(Field field, Object targetObject) throws IllegalAccessException {
        Object value = field.get(targetObject);
        return value instanceof Optional ? ((Optional<?>) value).orElse(null) : value;
    }

    private static FieldChange compareActiveField(Object sourceValue, Object targetValue, String fieldName) {
        Integer sourceActive = (Integer) sourceValue;
        Boolean targetActive = (Boolean) targetValue;

        if ((sourceActive == null && targetActive != null) ||
                (sourceActive != null && targetActive == null) ||
                (sourceActive != null && sourceActive.equals(1) != targetActive)) {
            return new FieldChange(fieldName, sourceValue, targetValue);
        }
        return null;
    }

    private static boolean valuesAreDifferent(Object sourceValue, Object targetValue) {
        if (targetValue == null) return false;

        if (sourceValue == null) return true;

        String sourceValueStr = String.valueOf(sourceValue);
        String targetValueStr = String.valueOf(targetValue);

        return !sourceValueStr.equals(targetValueStr);
    }

}


