package com.buaa.academic.scholar.client;

import com.buaa.academic.model.web.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("client-spider")
public interface SpiderClient {

    @PostMapping("/crawlWithUrl")
    Result<Void> url(@RequestHeader(name = "Auth") String userId,
                     @RequestParam(name = "url") String url);

    @PostMapping("/crawlWithTitle")
    Result<Void> title(@RequestHeader(name = "Auth") String userId,
                       @RequestParam(name = "title") String title);

}
