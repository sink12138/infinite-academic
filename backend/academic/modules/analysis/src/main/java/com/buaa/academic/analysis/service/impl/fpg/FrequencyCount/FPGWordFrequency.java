package com.buaa.academic.analysis.service.impl.fpg.FrequencyCount;

import com.buaa.academic.analysis.model.Bucket;
import lombok.NoArgsConstructor;
import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

@NoArgsConstructor
public class FPGWordFrequency extends Bucket implements WritableComparable<FPGWordFrequency> {

    public enum Counter {
        LINE_LEN                                                //用来保存总行数
    }

    public FPGWordFrequency(String word, int frequency) {
        super(word, frequency);
    }

    public String getTerm() {
        return super.getTerm();
    }

    public int getFrequency() {
        return super.getFrequency();
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        super.setTerm(in.readUTF());
        super.setFrequency(in.readInt());
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeUTF(super.getTerm());
        out.writeInt(super.getFrequency());
    }

    @Override
    public int compareTo(FPGWordFrequency o) {                            //重写compareTo方法，该类型排序时，按词频降序，当词频相同时，按单词字典序排序
        return (super.getFrequency() != o.getFrequency()) ? Integer.compare(o.getFrequency(), super.getFrequency())
                : super.getTerm().compareTo(o.getTerm());
    }

    @Override
    public String toString() {
        return super.getTerm() + "@@@" + super.getFrequency();
    }

}

