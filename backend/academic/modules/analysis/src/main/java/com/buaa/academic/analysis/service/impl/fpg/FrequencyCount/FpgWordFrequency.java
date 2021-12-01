package com.buaa.academic.analysis.service.impl.fpg.FrequencyCount;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import com.buaa.academic.analysis.model.WordFrequency;
import org.apache.hadoop.io.WritableComparable;

public class FpgWordFrequency extends WordFrequency implements WritableComparable<FpgWordFrequency>{

    public enum Counter{
        LINE_LEN												//用来保存总行数
    }

    public FpgWordFrequency() {
        super();
    }

    public FpgWordFrequency(String word, int frequence) {
        super(word,frequence);
    }

    public String getWord(){
        return super.getWord();
    }

    public int getFrequency(){
        return super.getFrequency();
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        super.setWord(in.readUTF());
        super.setFrequency(in.readInt());
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeUTF(super.getWord());
        out.writeInt(super.getFrequency());
    }

    @Override
    public int compareTo(FpgWordFrequency o) {							//重写compareTo方法，该类型排序时，按词频降序，当词频相同时，按单词字典序排序
        return (super.getFrequency() != o.getFrequency()) ? Integer.compare( o.getFrequency(), super.getFrequency())
                : super.getWord().compareTo(o.getWord());
    }

    @Override
    public String toString() {
        return super.getWord() + ":" + super.getFrequency();
    }

}

