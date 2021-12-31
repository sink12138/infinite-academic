package com.buaa.academic.tool.validator;

import org.springframework.scheduling.support.CronExpression;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class CronExprValidator implements ConstraintValidator<CronExpr, String> {

    private int order;

    @Override
    public void initialize(CronExpr cronExpr) {
        this.order = cronExpr.maxFreq().getOrder();
    }

    @Override
    public boolean isValid(String expression, ConstraintValidatorContext context) {
        if (expression == null)
            return true;
        if (!CronExpression.isValidExpression(expression))
            return false;
        String[] fields = expression.split("\\s+");
        if (fields.length != 6)
            return false;
        String tmp = fields[4];
        fields[4] = fields[5];
        fields[5] = tmp;
        if (!fields[4].equals("?"))
            fields[3] = "0";
        for (int i = 0; i < order; ++i) {
            if (!fields[i].equals("0"))
                return false;
        }
        return fields[order].matches("\\d+");
    }

}
