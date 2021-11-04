package com.buaa.academic.search.util;

import com.buaa.academic.search.model.Condition;
import org.hibernate.validator.constraints.Length;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class SearchConditionValidator implements ConstraintValidator<SearchCondition, Object> {

    @Override
    public void initialize(SearchCondition searchCondition) {
    }

    @Override
    public boolean isValid(Object object, ConstraintValidatorContext context) {
        if (object == null)
            return true;
        ArrayList<Condition> conditions = new ArrayList<>();
        if (object instanceof Condition condition) {
            conditions.add(condition);
        }
        else if (object instanceof Condition[] conditionArr) {
            conditions.addAll(Arrays.stream(conditionArr).toList());
        }
        else if (object instanceof Iterable iterable) {
            for (Object obj : iterable) {
                if (obj instanceof Condition condition) {
                    conditions.add(condition);
                }
                else return false;
            }
        }
        else return false;
        for (Condition condition : conditions) {
            if (condition == null)
                return false;
            if (condition.isCompound()) {
                condition.setKeyword(null);
                condition.setScope(null);
                condition.setFuzzy(false);
                condition.setTranslated(false);
                condition.setLanguages(null);
                if (!validateCompound(condition.getSubConditions()))
                    return false;
            }
            else {
                condition.setSubConditions(null);
                if (!condition.isTranslated())
                    condition.setLanguages(null);
                if (!validateSingle(condition.getKeyword(), condition.getScope()))
                    return false;
            }
        }
        return true;
    }

    private boolean validateCompound(@NotNull @Size(min = 2, max = 5) List<Condition> subConditions) {
        return !subConditions.isEmpty();
    }

    private boolean validateSingle(@NotNull @NotEmpty @Length(max = 32) String keyword,
                                   @NotNull @NotEmpty Set<String> scope) {
        return !keyword.isEmpty() && scope != null;
    }

}

