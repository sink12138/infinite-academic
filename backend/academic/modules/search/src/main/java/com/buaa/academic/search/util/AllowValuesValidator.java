package com.buaa.academic.search.util;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.*;
import java.util.stream.Collectors;

public class AllowValuesValidator implements ConstraintValidator<AllowValues, Object> {

    private Set<String> allowed;

    @Override
    public void initialize(AllowValues allowValues) {
        if (allowValues.value().length > 0)
            this.allowed = Arrays.stream(allowValues.value()).collect(Collectors.toSet());
        else for (int intValue : allowValues.intValue()) {
            this.allowed.add(String.valueOf(intValue));
        }
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null)
            return true;
        Set<String> values = new HashSet<>();
        if (value instanceof String string) {
            values.add(string);
        }
        else if (value instanceof Object[] objects) {
            for (Object obj : objects) {
                values.add(obj.toString());
            }
        }
        else if (value instanceof Iterable iter) {
            @SuppressWarnings("unchecked")
            Iterable<Object> objects = (Iterable<Object>) iter;
            objects.forEach(item -> values.add(item.toString()));
        }
        return this.allowed.containsAll(values);
    }

}
