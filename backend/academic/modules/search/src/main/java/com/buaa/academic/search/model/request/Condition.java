package com.buaa.academic.search.model.request;

import com.buaa.academic.search.dao.TextMappings;
import com.buaa.academic.tool.translator.Translator;
import com.buaa.academic.tool.validator.AllowValues;
import com.buaa.academic.search.validator.SearchCondition;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "查询条件模型")
public class Condition {

    @AllowValues({"and", "or", "not"})
    @ApiModelProperty(value = "条件的逻辑类型", required = true, allowableValues = "and,or,not", example = "and")
    private String logic;

    @ApiModelProperty(value = "是否为复合条件。若为true则必须指定子条件，若为false则必须指定除子条件外所有属性", required = true)
    private boolean compound;

    @Length(max = 128)
    @ApiModelProperty(value = "搜索关键词，超过64个字符将被截断，忽略头尾空白符", example = "机器学习")
    private String keyword;

    @ApiModelProperty(value = "限定关键词的出现范围，均为属性名", example = "title")
    private Set<@NotNull String> scope;

    @ApiModelProperty(value = "是否模糊搜索")
    private boolean fuzzy;

    @ApiModelProperty(value = "是否进行语种关联，若为true则需指定languages")
    private boolean translated;

    @AllowValues({"zh", "en"})
    @ApiModelProperty(value = "需要扩展的语言", allowableValues = "zh,en", example = "zh")
    private Set<@NotNull String> languages;

    @Size(min = 1, max = 5)
    @SearchCondition
    @ApiModelProperty(value = "查询子条件")
    private List<@NotNull Condition> subConditions;

    public QueryBuilder compile(String strategy) {
        if (compound) {
            BoolQueryBuilder builder = QueryBuilders.boolQuery();
            for (Condition subCond : subConditions) {
                switch (subCond.logic) {
                    case "and" -> builder.must(subCond.compile(strategy));
                    case "or" -> builder.should(subCond.compile(strategy));
                    case "not" -> builder.mustNot(subCond.compile(strategy));
                }
            }
            return builder;
        }
        else {
            String[] fields = scope.toArray(new String[0]);
            if (!fuzzy) {
                for (int i = 0; i < fields.length; ++i) {
                    if (TextMappings.contains(strategy, fields[i]))
                        fields[i] += ".raw";
                }
            }
            if (translated) {
                Set<String> keywords = new HashSet<>(6);
                keywords.add(keyword);
                for (String lang : languages) {
                    keywords.add(Translator.translate(keyword, "auto", lang));
                }
                BoolQueryBuilder builder = QueryBuilders.boolQuery();
                if (fuzzy) {
                    for (String key : keywords) {
                        builder.should(QueryBuilders.multiMatchQuery(key, fields));
                    }
                }
                else {
                    String[] keyArr = keywords.toArray(new String[0]);
                    for (String field : fields) {
                        builder.should(QueryBuilders.termsQuery(field, keyArr));
                    }
                }
                return builder;
            }
            else {
                if (fuzzy) {
                    return QueryBuilders.multiMatchQuery(keyword, fields);
                }
                else {
                    if (fields.length > 1) {
                        BoolQueryBuilder builder = QueryBuilders.boolQuery();
                        for (String field : fields) {
                            builder.should(QueryBuilders.termQuery(field, keyword));
                        }
                        return builder;
                    }
                    else {
                        return QueryBuilders.termQuery(fields[0], keyword);
                    }
                }
            }
        }
    }

}
