package com.buaa.academic.analysis.service.impl.fpg.FrequencyCount;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import com.buaa.academic.analysis.model.Frequency;
import org.apache.hadoop.io.WritableComparable;

public class FpgWordFrequency extends Frequency implements WritableComparable<FpgWordFrequency>{

    public enum Counter{
        LINE_LEN												//用来保存总行数
    }

    public FpgWordFrequency() {
        super();
    }

    public FpgWordFrequency(String word, int frequence) {
        super(word,frequence);
    }

    public String getName(){
        return super.getName();
    }

    public int getFrequency(){
        return super.getFrequency();
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        super.setName(in.readUTF());
        super.setFrequency(in.readInt());
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeUTF(super.getName());
        out.writeInt(super.getFrequency());
    }

    @Override
    public int compareTo(FpgWordFrequency o) {							//重写compareTo方法，该类型排序时，按词频降序，当词频相同时，按单词字典序排序
        return (super.getFrequency() != o.getFrequency()) ? Integer.compare( o.getFrequency(), super.getFrequency())
                : super.getName().compareTo(o.getName());
    }

    @Override
    public String toString() {
        return super.getName() + ":" + super.getFrequency();
    }

}

