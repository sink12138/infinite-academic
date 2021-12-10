package com.buaa.academic.document.system;

import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public enum ApplicationType {

    CERTIFICATION("学者认证"),
    CLAIM("门户认领"),
    MODIFICATION("门户信息修改"),
    NEW_PAPER("添加论文"),
    REMOVE_PAPER("下架论文"),
    UPDATE_PAPER("修改论文信息"),
    TRANSFER("专利转让");

    @JsonValue
    private final String value;

    public String getValue() {
        return this.value;
    }

    ApplicationType(String value) {
        this.value = value;
    }

    @WritingConverter
    public static class EnumToStringConverter implements Converter<ApplicationType, String> {

        @Override
        @Nullable
        public String convert(@Nullable ApplicationType source) {
            if (source == null)
                return null;
            return source.value;
        }

    }

    @ReadingConverter
    public static class StringToEnumConverter implements Converter<String, ApplicationType> {

        @Override
        @Nullable
        public ApplicationType convert(@NonNull String source) {
            for (ApplicationType type : ApplicationType.values()) {
                if (type.value.equals(source)) {
                    return type;
                }
            }
            return null;
        }
    }

}
