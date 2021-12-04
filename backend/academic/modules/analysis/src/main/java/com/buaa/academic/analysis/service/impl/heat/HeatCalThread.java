package com.buaa.academic.analysis.service.impl.heat;

import com.buaa.academic.analysis.dao.SubjectRepository;
import com.buaa.academic.analysis.dao.TopicRepository;
import com.buaa.academic.analysis.service.impl.JobType;
import com.buaa.academic.analysis.service.impl.StatusCtrl;
import com.buaa.academic.document.statistic.Subject;
import com.buaa.academic.document.statistic.NumPerYear;
import com.buaa.academic.document.statistic.Topic;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HeatCalThread implements Runnable{
    private final String threadName;
    private TopicRepository topicRepository;
    private SubjectRepository subjectRepository;

    public HeatCalThread(String threadName) {
        this.threadName = threadName;
    }

    public HeatCalThread setTopicRepository(TopicRepository topicRepository) {
        this.topicRepository = topicRepository;
        return this;
    }

    public HeatCalThread setSubjectRepository(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
        return this;
    }

    @Override
    public void run() {
        while (true){

            if (StatusCtrl.isStopped(threadName))
                return;

            Terms.Bucket bucket;

            synchronized (StatusCtrl.STATUS_LOCK) {
                int index = HeatUpdateMainThread.finished.get(threadName);
                int total = HeatUpdateMainThread.total.get(threadName);
                if (index >= total)
                    return;
                bucket = HeatUpdateMainThread.targetTerm.getBuckets().get(index);
                HeatUpdateMainThread.finished.put(threadName, ++index);
                StatusCtrl.runningStatus.put(threadName, "Statics analysis["+index+"/"+total+"]...");
            }

            String targetName = bucket.getKey().toString();
            double heat = 0.0;
            Aggregation aggregationYear = bucket.getAggregations().get("year_term");
            ParsedLongTerms longTerms = (ParsedLongTerms) aggregationYear;
            NumPerYear publicationData = new NumPerYear();
            List<Integer> years = new ArrayList<>();
            List<Integer> numbers = new ArrayList<>();
            for (Terms.Bucket yearBucket : longTerms.getBuckets()) {
                Double rate = HeatUpdateMainThread.rate.get(Integer.parseInt(yearBucket.getKey().toString()));
                if (rate != null)
                    heat += yearBucket.getDocCount() * rate;
                years.add(Integer.parseInt(yearBucket.getKey().toString()));
                numbers.add((int)yearBucket.getDocCount());
            }
            publicationData.setYears(years);
            publicationData.setNums(numbers);
            if (Objects.equals(threadName, JobType.HOT_TOPIC_ANALYSIS.name())) {
                Topic topic = topicRepository.findTopicByName(targetName);
                assert topic != null;
                topic.setHeat(heat);
                topic.setPubsPerYear(publicationData);
                topicRepository.save(topic);
            } else {
                Subject subject = subjectRepository.findSubjectByName(targetName);
                assert subject != null;
                subject.setHeat(heat);
                subject.setPubsPerYear(publicationData);
                subjectRepository.save(subject);
            }
        }
    }
}
