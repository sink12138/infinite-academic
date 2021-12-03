package com.buaa.academic.admin.client;

import com.buaa.academic.model.web.Result;
import com.buaa.academic.model.web.Schedule;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("client-analysis")
public interface AnalysisClient {

    @GetMapping("/schedule/status")
    Result<Schedule> status(@RequestHeader(name = "Auth") String auth);

    @PostMapping("/schedule/start")
    Result<Void> start(@RequestHeader(name = "Auth") String auth);

    @PostMapping("/schedule/stop")
    Result<Void> stop(@RequestHeader(name = "Auth") String auth);

    @PostMapping("/schedule/timing")
    Result<Void> timing(@RequestHeader(name = "Auth") String auth,
                        @RequestParam(value = "cron") String cron);

}
