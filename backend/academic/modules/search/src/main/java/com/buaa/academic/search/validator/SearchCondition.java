package com.buaa.academic.search.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Check if the condition is a valid search condition
 * (its keyword and scope must be specified, unless it is compound and has sub-conditions).
 */
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_PARAMETER, ElementType.TYPE_USE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SearchConditionValidator.class)
public @interface SearchCondition {

    @Deprecated
    String[] value() default {};

    @Deprecated
    String message() default "Not a valid search condition";

    @Deprecated
    Class<?>[] groups() default {};

    @Deprecated
    Class<? extends Payload>[] payload() default {};

}
