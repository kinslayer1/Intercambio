package avvillas.template_admin.validation.validator;

import avvillas.template_admin.validation.annotation.OptionalNotEmpty;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class OptionalNotEmptyValidator implements ConstraintValidator<OptionalNotEmpty, Optional<?>> {

    @Override
    public void initialize(OptionalNotEmpty constraintAnnotation) {
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean isValid(Optional<?> value, ConstraintValidatorContext context) {
        if (value.isEmpty()) {
            return true;
        }

        Object unwrapped = value.get();

        if (unwrapped instanceof String) {
            return !((String) unwrapped).trim().isEmpty();
        }

        if (unwrapped instanceof Collection) {
            return !((Collection) unwrapped).isEmpty();
        }

        if (unwrapped instanceof Map) {
            return !((Map) unwrapped).isEmpty();
        }

        if (unwrapped.getClass().isArray()) {
            return java.lang.reflect.Array.getLength(unwrapped) > 0;
        }

        if (unwrapped instanceof Boolean) {
            return true;
        }


        if (unwrapped instanceof Number) {
            return true;
        }

        return unwrapped != null;
    }
}