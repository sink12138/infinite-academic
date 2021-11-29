package com.buaa.academic.search.validator;

import com.buaa.academic.search.model.request.Filter;
import com.buaa.academic.search.model.request.Filter.*;
import com.buaa.academic.tool.util.StringUtils;

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
            FilterFormat format = filter.getFormat();
            FilterType type = filter.getType();
            if (format == null || type == null)
                return false;
            switch (format) {
                case NUMERIC -> {
                    filter.setKeyParams(null);
                    int[] params = filter.getIntParams();
                    // Existence checks of params
                    switch (type) {
                        case BELOW, ABOVE, RANGE, EQUAL -> {
                            if (params == null)
                                return false;
                        }
                        case TRUE, FALSE -> filter.setIntParams(null);
                        default -> {
                            return false;
                        }
                    }
                    // Length checks of params
                    switch (type) {
                        case BELOW, ABOVE, EQUAL -> {
                            if (params.length < 1)
                                return false;
                        }
                        case RANGE -> {
                            if (params.length < 2)
                                return false;
                        }
                    }
                }
                case DISCRETE -> {
                    if (!type.equals(FilterType.EQUAL))
                        return false;
                    filter.setIntParams(null);
                    String[] params = filter.getKeyParams();
                    if (params == null || params.length < 1)
                        return false;
                    for (int i = 0; i < params.length; ++i) {
                        if (params[i] == null)
                            return false;
                        params[i] = StringUtils.strip(params[i], 32);
                        if (params[i].isEmpty())
                            return false;
                    }
                }
                default -> {
                    return false;
                }
            }
        }
        return true;
    }

}
