package com.buaa.academic.analysis.service.impl.hot;

import com.buaa.academic.analysis.repository.SubjectRepository;
import com.buaa.academic.analysis.repository.TopicRepository;
import com.buaa.academic.analysis.service.impl.JobType;
import com.buaa.academic.analysis.service.impl.StatusCtrl;
import com.buaa.academic.document.statistic.PublicationData;
import com.buaa.academic.document.statistic.Subject;
import com.buaa.academic.document.statistic.Topic;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;

import java.util.ArrayList;
import java.util.Objects;

public class HotCalThread implements Runnable{
    private final String threadName;
    private TopicRepository topicRepository;
    private SubjectRepository subjectRepository;

    public HotCalThread(String threadName) {
        this.threadName = threadName;
    }

    public HotCalThread setTopicRepository(TopicRepository topicRepository) {
        this.topicRepository = topicRepository;
        return this;
    }

    public HotCalThread setSubjectRepository(SubjectRepository subjectRepository) {
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
                int index = HotUpdateMainThread.finished.get(threadName);
                int total = HotUpdateMainThread.total.get(threadName);
                if (index >= total)
                    return;
                bucket = HotUpdateMainThread.targetTerm.getBuckets().get(index);
                HotUpdateMainThread.finished.put(threadName, ++index);
                StatusCtrl.runningStatus.put(threadName, "Statics analysis["+index+"/"+total+"]...");
            }

            String targetName = bucket.getKey().toString();
            double hot = 0.0;
            Aggregation aggregationYear = bucket.getAggregations().get("year_term");
            ParsedLongTerms longTerms = (ParsedLongTerms) aggregationYear;
            ArrayList<PublicationData> publicationData = new ArrayList<>();
            for (Terms.Bucket yearBucket : longTerms.getBuckets()) {
                hot += yearBucket.getDocCount() * HotUpdateMainThread.rate.get(Integer.parseInt(yearBucket.getKey().toString()));
                publicationData.add(new PublicationData(Integer.parseInt(yearBucket.getKey().toString()), (int)yearBucket.getDocCount()));
            }

            if (Objects.equals(threadName, JobType.HOT_TOPIC_ANALYSIS.name())) {
                Topic topic = topicRepository.findTopicByName(targetName);
                topic.setHeat(hot);
                topic.setPublicationData(publicationData);
                topicRepository.save(topic);
            } else {
                Subject subject = subjectRepository.findSubjectByName(targetName);
                subject.setHeat(hot);
                subject.setPublicationData(publicationData);
                subjectRepository.save(subject);
            }
        }
    }
}
