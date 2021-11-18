package com.buaa.academic.analysis.service.impl.fpg.FrequencyCount;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.WritableComparable;

public class WordFrequency implements WritableComparable<WordFrequency>{

    private String word;										//单词
    private int frequency;//词频

    public enum Counter{
        LINE_LEN												//用来保存总行数
    }

    public WordFrequency() {}

    public WordFrequency(String word, int frequence) {
        super();
        this.word = word;
        this.frequency = frequence;
    }

    public String getWord(){
        return word;
    }

    public int getFrequency(){
        return frequency;
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        word = in.readUTF();
        frequency = in.readInt();
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeUTF(word);
        out.writeInt(frequency);
    }

    @Override
    public int compareTo(WordFrequency o) {							//重写compareTo方法，该类型排序时，按词频降序，当词频相同时，按单词字典序排序
        return (frequency != o.frequency) ? Integer.compare(o.frequency, frequency)
                : word.compareTo(o.word);
    }

    @Override
    public String toString() {
        return word + ":" + frequency;
    }

}

