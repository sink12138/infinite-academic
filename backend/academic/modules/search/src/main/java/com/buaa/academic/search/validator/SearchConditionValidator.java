package com.buaa.academic.search.validator;

import com.buaa.academic.search.model.request.Condition;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.NotBlank;
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
                String keyword = condition.getKeyword();
                if (keyword == null)
                    return false;
                keyword = keyword.trim().replaceAll("\\s+", " ");
                if (keyword.length() > 32)
                    keyword = keyword.substring(0, 32);
                condition.setKeyword(keyword);
                if (condition.getScope() == null || condition.getScope().isEmpty())
                    return false;
                if (!condition.isTranslated())
                    condition.setLanguages(null);
                else if (condition.getLanguages() == null || condition.getLanguages().isEmpty())
                    return false;
                if (!validateSingle(condition.getKeyword(), condition.getScope()))
                    return false;
            }
        }
        return true;
    }

    private boolean validateCompound(@NotNull @Size(min = 2, max = 5) List<Condition> subConditions) {
        return !subConditions.isEmpty();
    }

    private boolean validateSingle(@NotNull @NotBlank String keyword,
                                   @NotNull @NotEmpty Set<@NotNull @NotBlank String> scope) {
        return !keyword.isEmpty() && scope != null;
    }

}

