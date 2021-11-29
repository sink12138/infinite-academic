package com.buaa.academic.search.model.request;

import com.buaa.academic.search.dao.TextMappings;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "过滤条件模型")
public class Filter {

    public enum FilterFormat {

        NUMERIC("numeric"), DISCRETE("discrete");

        @JsonValue
        private final String value;

        public String getValue() {
            return value;
        }

        FilterFormat(String value) {
            this.value = value;
        }

    }

    public enum FilterType {

        BELOW("below"), ABOVE("above"), RANGE("range"),
        EQUAL("equal"), TRUE("true"), FALSE("false");

        @JsonValue
        private final String value;

        public String getValue() {
            return value;
        }

        FilterType(String value) {
            this.value = value;
        }

    }

    @ApiModelProperty(
            value = "过滤器格式，可用值为<b>numeric</b>（数值型）和<b>discrete</b>（离散型）",
            required = true,
            example = "numeric")
    private FilterFormat format;

    @ApiModelProperty(
            value = "过滤器类型，可用值如下：</br>" +
                    "<b>below</b> - 属性值小于等于给定参数（params[0]），例如出版年份</br>" +
                    "<b>above</b> - 属性值大于等于给定参数（params[0]），例如出版年份、被引量</br>" +
                    "<b>range</b> - 属性值在给定范围之间（params[0]和params[1]，含两端），例如出版年份</br>" +
                    "<b>equal</b> - 属性值等于给定参数（params）之一，例如出版年份</br>" +
                    "<b>true</b> - 属性值为真，例如可下载</br>" +
                    "<b>false</b> - 属性值为假，暂时没想到能用来干啥</br>" +
                    "注意：若format = discrete，则只允许设置type = equal。",
            required = true,
            example = "above")
    private FilterType type;

    @ApiModelProperty(value = "待过滤的属性名", required = true, example = "year")
    private String attr;

    @ApiModelProperty(value = "过滤器数值参数列表（format = numeric且type不为布尔类型时，需要设定此参数）", example = "2016")
    private int[] intParams;

    @ApiModelProperty(value = "过滤器字符串参数列表（format = discrete时，需要设定此参数）", example = "2016")
    private String[] keyParams;

    public QueryBuilder compile(String strategy) {
        switch (type) {
            case BELOW -> {
                return QueryBuilders.rangeQuery(attr).lte(intParams[0]);
            }
            case ABOVE -> {
                return QueryBuilders.rangeQuery(attr).gte(intParams[0]);
            }
            case RANGE -> {
                return QueryBuilders.rangeQuery(attr).gte(intParams[0]).lte(intParams[1]);
            }
            case EQUAL -> {
                if (format.equals(FilterFormat.NUMERIC))
                    return QueryBuilders.termsQuery(attr, intParams);
                else {
                    if (TextMappings.contains(strategy, attr)) {
                        attr += ".raw";
                    }
                    return QueryBuilders.termsQuery(attr, keyParams);
                }
            }
            case TRUE -> {
                return QueryBuilders.termQuery(attr, true);
            }
            case FALSE -> {
                return QueryBuilders.termQuery(attr, false);
            }
        }
        return null;
    }

}
