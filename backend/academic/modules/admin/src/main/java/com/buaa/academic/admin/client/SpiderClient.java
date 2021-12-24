package com.buaa.academic.admin.client;

import com.buaa.academic.model.web.Result;
import com.buaa.academic.model.web.Schedule;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient("client-spider")
public interface SpiderClient {

    @PostMapping("/start")
    Result<Void> start(@RequestHeader(name = "Auth") String auth);

    @PostMapping("/stop")
    Result<Void> stop(@RequestHeader(name = "Auth") String auth);

    @GetMapping("/status")
    Result<Schedule> status(@RequestHeader(name = "Auth") String auth);

    @PostMapping("/timing")
    Result<Void> timing(@RequestHeader(name = "Auth") String auth,
                        @RequestParam(value = "cron") String cron);

    @PostMapping("/setting")
    Result<Void> inspire(@RequestHeader(name = "Auth") String auth,
                         @RequestBody List<String> inspirations);

}
