package com.buaa.academic.analysis.service.impl.fpg.AssociationCal;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class AssociationReducer extends Reducer<LongWritable, Text, Text, Text> {
}
