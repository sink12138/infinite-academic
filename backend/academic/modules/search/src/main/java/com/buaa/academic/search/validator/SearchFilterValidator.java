package com.buaa.academic.search.validator;

import com.buaa.academic.search.model.request.Filter;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.Arrays;

public class SearchFilterValidator implements ConstraintValidator<SearchFilter, Object> {

    @Override
    public void initialize(SearchFilter searchFilter) {
    }

    @Override
    public boolean isValid(Object object, ConstraintValidatorContext context) {
        if (object == null)
            return true;
        ArrayList<Filter> filters = new ArrayList<>();
        if (object instanceof Filter filter) {
            filters.add(filter);
        }
        else if (object instanceof Filter[] filterArr) {
            filters.addAll(Arrays.stream(filterArr).toList());
        }
        else if (object instanceof Iterable iterable) {
            for (Object obj : iterable) {
                if (obj instanceof Filter filter) {
                    filters.add(filter);
                }
                else return false;
            }
        }
        else return false;
        for (Filter filter : filters) {
            if (filter == null)
                return false;
            // Attr checks
            if (filter.getAttr() == null || !filter.getAttr().matches("^[a-z][a-zA-Z_0-9]*$"))
                return false;
            String type = filter.getType();
            if (type == null)
                return false;
            int[] params = filter.getParams();
            // Existence checks of params
            switch (type) {
                case "below", "above", "range", "equal" -> {
                    if (params == null)
                        return false;
                }
                case "true", "false" -> filter.setParams(null);
                default -> {
                    return false;
                }
            }
            // Length checks of params
            switch (type) {
                case "below", "above", "equal" -> {
                    if (params.length < 1)
                        return false;
                }
                case "range" -> {
                    if (params.length < 2)
                        return false;
                }
            }
        }
        return true;
    }

}
