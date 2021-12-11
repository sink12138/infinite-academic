package com.buaa.academic.scholar.client;

import com.buaa.academic.model.web.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("client-resource")
public interface ResourceClient {

    @GetMapping("/exists")
    Result<Boolean> exists(@RequestParam(name = "token") String token);

}
