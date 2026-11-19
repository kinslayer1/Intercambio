package avvillas.template_admin.validation.validator;

import avvillas.template_admin.validation.annotation.OptionalPattern;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Optional;
import java.util.regex.Pattern;

public class OptionalPatternValidator implements ConstraintValidator<OptionalPattern, Optional<String>> {
    private Pattern pattern;

    @Override
    public void initialize(OptionalPattern constraintAnnotation) {
        int flags = constraintAnnotation.caseInsensitive() ? Pattern.CASE_INSENSITIVE : 0;
        this.pattern = Pattern.compile(constraintAnnotation.regexp(), flags);
    }

    @Override
    public boolean isValid(Optional<String> value, ConstraintValidatorContext context) {
        return value.map(pattern::matcher)
                .map(matcher -> matcher.matches())
                .orElse(true);
    }
}