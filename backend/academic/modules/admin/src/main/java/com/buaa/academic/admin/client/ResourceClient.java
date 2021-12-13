package com.buaa.academic.admin.client;

import com.buaa.academic.model.web.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("client-resource")
public interface ResourceClient {

    @PostMapping("/remove")
    Result<Void> remove(@RequestParam(name = "token") String token);

}
