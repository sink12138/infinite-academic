package com.buaa.academic.analysis.service.impl.fpg.AssociationCal;

import org.apache.hadoop.io.Writable;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class AssociationRule implements Writable, Comparable<AssociationRule> {

    private String item;
    private String associationItems;
    private double confidence;

    public AssociationRule() {}

    public AssociationRule(String item, String associationItems, double confidence) {
        this.item = item;
        this.associationItems = associationItems;
        this.confidence = confidence;
    }

    public String getItem() {
        return item;
    }

    public String getAssociationItems() {
        return associationItems;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public void setAssociationItems(String associationItems) {
        this.associationItems = associationItems;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeUTF(item);
        dataOutput.writeUTF(associationItems);
        dataOutput.writeDouble(confidence);
    }

    @Override
    public void readFields(DataInput dataInput) throws IOException {
        item = dataInput.readUTF();
        associationItems = dataInput.readUTF();
        confidence = dataInput.readDouble();
    }

    @Override
    public int compareTo(AssociationRule o) {
        return o.getConfidence() == this.confidence ?
                o.getAssociationItems().compareTo( this.associationItems) : Double.compare(o.getConfidence(),  this.confidence);
    }
}
