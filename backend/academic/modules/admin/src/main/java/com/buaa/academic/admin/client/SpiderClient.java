package com.buaa.academic.admin.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("client-spider")
public interface SpiderClient { }
