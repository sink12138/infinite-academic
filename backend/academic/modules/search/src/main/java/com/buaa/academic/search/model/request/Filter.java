package com.buaa.academic.search.model.request;

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

    @ApiModelProperty(
            value = "过滤器类型，可用值如下：</br>" +
                    "<b>below</b> - 属性值小于等于给定参数（params[0]），例如出版年份</br>" +
                    "<b>above</b> - 属性值大于等于给定参数（params[0]），例如出版年份、被引量</br>" +
                    "<b>range</b> - 属性值在给定范围之间（params[0],和params[1]含两端），例如出版年份</br>" +
                    "<b>equal</b> - 属性值等于给定参数（params）之一，例如出版年份</br>" +
                    "<b>true</b> - 属性值为真，例如可下载</br>" +
                    "<b>false</b> - 属性值为假，暂时没想到能用来干啥</br>",
            required = true,
            example = "above")
    private String type;

    @ApiModelProperty(value = "待过滤的属性名", required = true, example = "year")
    private String attr;

    @ApiModelProperty(value = "过滤器参数列表（布尔类型的过滤器不需要设定参数）", example = "2016")
    private int[] params;

    public QueryBuilder compile() {
        switch (type) {
            case "below" -> {
                return QueryBuilders.rangeQuery(attr).lte(params[0]);
            }
            case "above" -> {
                return QueryBuilders.rangeQuery(attr).gte(params[0]);
            }
            case "range" -> {
                return QueryBuilders.rangeQuery(attr).gte(params[0]).lte(params[1]);
            }
            case "equal" -> {
                return QueryBuilders.termsQuery(attr, params);
            }
            case "true" -> {
                return QueryBuilders.termQuery(attr, true);
            }
            case "false" -> {
                return QueryBuilders.termQuery(attr, false);
            }
        }
        return null;
    }

}
