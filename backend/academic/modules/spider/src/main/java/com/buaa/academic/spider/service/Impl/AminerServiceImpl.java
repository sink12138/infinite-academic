package com.buaa.academic.spider.service.Impl;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.buaa.academic.document.entity.Institution;
import com.buaa.academic.document.entity.Journal;
import com.buaa.academic.document.entity.Researcher;
import com.buaa.academic.spider.service.AminerService;
import com.buaa.academic.spider.util.CacheMap;
import org.elasticsearch.index.query.QueryBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

@Service
public class AminerServiceImpl implements AminerService {

    private final String baseDir = "/data/tmp/aminer/";

    private static final CacheMap<String, File> researcherCache = new CacheMap<>(65536);

    private static final CacheMap<String, String> journalCache = new CacheMap<>(65536);

    @Autowired
    private ElasticsearchRestTemplate template;

    private final Logger log = LoggerFactory.getLogger(AminerService.class);

    @Override
    public Researcher findResearcher(String researcherId) throws IOException {
        String key = researcherId.substring(0, 20);
        File target;
        synchronized (researcherCache) {
            target = researcherCache.get(key);
            if (target == null) {
                target = new File(baseDir, "researchers/" + key);
                if (!target.exists())
                    return null;
                researcherCache.put(key, target);
            }
        }
        synchronized (researcherCache.get(key)) {
            Scanner scanner = new Scanner(target);
            Researcher researcher = null;
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                try {
                    if (line.length() >= 32 && line.substring(8, 32).equals(researcherId)) {
                        StringBuilder builder = new StringBuilder(line);
                        while (!line.endsWith("}") && scanner.hasNext()) {
                            builder.append(scanner.nextLine());
                        }
                        line = builder.toString();
                        researcher = JSONObject.parseObject(line, Researcher.class);
                        break;
                    }
                }
                catch (Exception exception) {
                    log.warn(exception.getMessage());
                    log.warn("JSON source: {}", line);
                }
            }
            scanner.close();
            return researcher;
        }
    }

    @Override
    public synchronized Journal findJournal(String journalId) throws IOException {
        String line = journalCache.get(journalId);
        if (line == null) {
            File target = new File(baseDir, "journals/" + journalId);
            if (!target.exists())
                return null;
            Scanner scanner = new Scanner(target);
            line = scanner.nextLine();
            StringBuilder builder = new StringBuilder(line);
            while (!line.endsWith("}") && scanner.hasNext()) {
                builder.append(scanner.nextLine());
            }
            line = builder.toString();
            scanner.close();
            journalCache.put(journalId, line);
        }
        try {
            return JSONObject.parseObject(line, Journal.class);
        }
        catch (JSONException exception) {
            log.warn(exception.getMessage());
            log.warn("JSON source: {}", line);
            return null;
        }
    }

    @Override
    public synchronized Institution findInstitution(String name) {
        SearchHit<Institution> hit = template.searchOne(new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.termQuery("name.raw", name))
                .build(), Institution.class);
        if (hit != null)
            return hit.getContent();
        Institution institution = new Institution();
        institution.setName(name);
        template.save(institution);
        return institution;
    }

}
