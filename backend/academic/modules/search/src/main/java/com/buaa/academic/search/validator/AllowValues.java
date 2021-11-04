package com.buaa.academic.search.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Check if the value(s) is among the given allowed values.
 */
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_PARAMETER, ElementType.TYPE_USE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AllowValuesValidator.class)
public @interface AllowValues {

    /**
     * Allowed values as string.
     */
    String[] value() default {};

    /**
     * Allowed values as integer. Will not be used if 'value' is already specified.
     */
    int[] intValue() default {};

    @Deprecated
    String message() default "Value not allowed";

    @Deprecated
    Class<?>[] groups() default {};

    @Deprecated
    Class<? extends Payload>[] payload() default {};

}
