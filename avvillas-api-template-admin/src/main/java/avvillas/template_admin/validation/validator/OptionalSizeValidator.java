package avvillas.template_admin.validation.validator;

import avvillas.template_admin.validation.annotation.OptionalSize;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Optional;

public class OptionalSizeValidator implements ConstraintValidator<OptionalSize, Optional<String>> {
    private int min;
    private int max;

    @Override
    public void initialize(OptionalSize constraintAnnotation) {
        this.min = constraintAnnotation.min();
        this.max = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(Optional<String> value, ConstraintValidatorContext context) {
        return value.map(s -> {
            if (s == null) return false;
            int length = s.length();
            return length >= min && length <= max;
        }).orElse(true);
    }
}