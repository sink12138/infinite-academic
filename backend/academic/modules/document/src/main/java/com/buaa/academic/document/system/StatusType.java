package com.buaa.academic.document.system;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Getter
public enum StatusType {

    UNDER_REVIEW("审核中"), PASSED("审核通过"), NOT_PASSED("审核不通过");

    @JsonValue
    private final String description;

    StatusType(String description) {
        this.description = description;
    }
    @WritingConverter
    public static class EnumToStringConverter implements Converter<StatusType, String> {

        @Override
        @Nullable
        public String convert(@Nullable StatusType source) {
            if (source == null)
                return null;
            return source.description;
        }

    }

    @ReadingConverter
    public static class StringToEnumConverter implements Converter<String, StatusType> {

        @Override
        @Nullable
        public StatusType convert(@NonNull String source) {
            for (StatusType type : StatusType.values()) {
                if (type.description.equals(source)) {
                    return type;
                }
            }
            return null;
        }
    }

}
