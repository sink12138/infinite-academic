package com.buaa.academic.tool.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CronExprValidator.class)
public @interface CronExpr {

    @Deprecated
    String[] value() default {};

    CronFrequency maxFreq() default CronFrequency.DAILY;

    @Deprecated
    String message() default "Not a valid cron expression";

    @Deprecated
    Class<?>[] groups() default {};

    @Deprecated
    Class<? extends Payload>[] payload() default {};

}
