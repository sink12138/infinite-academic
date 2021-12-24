package com.buaa.academic.analysis.model;


import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "五大实体总量")
public class TotalCounts {

    private String papers;

    private String researchers;

    private String institutions;

    private String journals;

    private String patents;

    public void setPapers(long count) {
        this.papers = splitNumber(count);
    }

    public void setResearchers(long count) {
        this.researchers = splitNumber(count);
    }

    public void setInstitutions(long count) {
        this.institutions = splitNumber(count);
    }

    public void setJournals(long count) {
        this.journals = splitNumber(count);
    }

    public void setPatents(long count) {
        this.patents = splitNumber(count);
    }

    private static String splitNumber(long num) {
        String number = String.valueOf(num);
        StringBuilder builder = new StringBuilder(number);
        for (int i = number.length() - 3; i >= 0; i -= 3) {
            builder.insert(i, ",");
        }
        return builder.toString();
    }


}
