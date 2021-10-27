package com.buaa.academic.document.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "journal")
public class Journal {

    @Id
    @Field(type = FieldType.Auto)
    private String id;

    @Field(type = FieldType.Keyword)
    private String journalId;

    @Field(type = FieldType.Text, analyzer = "ik_optimized", searchAnalyzer = "ik_optimized")
    private String title;

    @Field(type = FieldType.Binary, index = false)
    private byte[] avatar;

}
