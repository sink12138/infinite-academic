package com.buaa.academic.analysis.service.impl.hot;

import com.buaa.academic.analysis.repository.SubjectDao;
import com.buaa.academic.analysis.repository.SubjectRepository;
import com.buaa.academic.analysis.repository.TopicDao;
import com.buaa.academic.analysis.repository.TopicRepository;
import com.buaa.academic.analysis.service.impl.JobType;
import com.buaa.academic.analysis.service.impl.StatusCtrl;
import com.buaa.academic.document.statistic.SumPerYear;
import com.buaa.academic.document.statistic.Subject;
import com.buaa.academic.document.statistic.Topic;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HotCalThread implements Runnable{
    private final String threadName;
    private TopicRepository topicRepository;
    private SubjectRepository subjectRepository;
    private ElasticsearchRestTemplate template;

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

    public HotCalThread setTemplate(ElasticsearchRestTemplate template) {
        this.template = template;
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
            SumPerYear publicationData = new SumPerYear();
            List<Integer> years = new ArrayList<>();
            List<Integer> numbers = new ArrayList<>();
            for (Terms.Bucket yearBucket : longTerms.getBuckets()) {
                Double rate = HotUpdateMainThread.rate.get(Integer.parseInt(yearBucket.getKey().toString()));
                if (rate != null)
                    hot += yearBucket.getDocCount() * rate;
                years.add(Integer.parseInt(yearBucket.getKey().toString()));
                numbers.add((int)yearBucket.getDocCount());
            }
            publicationData.setYears(years);
            publicationData.setNums(numbers);
            if (Objects.equals(threadName, JobType.HOT_TOPIC_ANALYSIS.name())) {
                Topic topic = TopicDao.getTopicByName(targetName, template);
                assert topic != null;
                topic.setHeat(hot);
                topic.setPublicationData(publicationData);
                topicRepository.save(topic);
            } else {
                Subject subject = SubjectDao.getSubjectByName(targetName, template);
                assert subject != null;
                subject.setHeat(hot);
                subject.setPublicationData(publicationData);
                subjectRepository.save(subject);
            }
        }
    }
}
