package com.buaa.academic.document.system;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Getter
public enum ApplicationType {

    CERTIFICATION("学者认证"),
    CLAIM("门户认领"),
    MODIFICATION("门户修改"),
    NEW_PAPER("添加论文"),
    EDIT_PAPER("修改论文"),
    REMOVE_PAPER("移除论文"),
    TRANSFER("专利转让");

    @JsonValue
    private final String description;

    ApplicationType(String description) {
        this.description = description;
    }

    @WritingConverter
    public static class EnumToStringConverter implements Converter<ApplicationType, String> {

        @Override
        @Nullable
        public String convert(@Nullable ApplicationType source) {
            if (source == null)
                return null;
            return source.description;
        }

    }

    @ReadingConverter
    public static class StringToEnumConverter implements Converter<String, ApplicationType> {

        @Override
        @Nullable
        public ApplicationType convert(@NonNull String source) {
            for (ApplicationType type : ApplicationType.values()) {
                if (type.description.equals(source)) {
                    return type;
                }
            }
            return null;
        }
    }

}
