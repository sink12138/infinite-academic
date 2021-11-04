package com.buaa.academic.search.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Check if the filter is theoretically, but only theoretically, a valid search filter.
 */
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_PARAMETER, ElementType.TYPE_USE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SearchFilterValidator.class)
public @interface SearchFilter {

    @Deprecated
    String[] value() default {};

    @Deprecated
    String message() default "Not a valid search filter";

    @Deprecated
    Class<?>[] groups() default {};

    @Deprecated
    Class<? extends Payload>[] payload() default {};

}
