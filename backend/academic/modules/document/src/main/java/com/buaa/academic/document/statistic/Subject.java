package com.buaa.academic.document.statistic;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;
import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "subject")
@Setting(settingPath = "settings.json")
public class Subject {

    @Id
    @Field(type = FieldType.Keyword)
    private String id;

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "ik_optimized_max_word", searchAnalyzer = "ik_optimized"),
            otherFields = @InnerField(suffix = "raw", type = FieldType.Keyword))
    private String name;

    @Field(type = FieldType.Double)
    private double heat;

    @Field(type = FieldType.Object)
    private ArrayList<Association> associationSubjects = new ArrayList<>();

    @Field(type = FieldType.Object)
    private ArrayList<DataPerYear> publicationData = new ArrayList<>();
}
