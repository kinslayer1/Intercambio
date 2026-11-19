package avvillas.template_admin.validation.annotation;

import avvillas.template_admin.validation.validator.OptionalNotEmptyValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = OptionalNotEmptyValidator.class)
public @interface OptionalNotEmpty {
    String message() default "El campo no puede estar vacío cuando está presente";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};


    @Target({ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        OptionalNotEmpty[] value();
    }
}