package com.buaa.academic.gateway.filter;

import com.alibaba.fastjson.JSONObject;
import com.buaa.academic.model.exception.AcademicException;
import com.buaa.academic.model.exception.ExceptionType;
import com.buaa.academic.model.security.Authority;
import com.buaa.academic.model.web.Result;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;

@Component
public class AuthorityFilter implements GlobalFilter, Ordered {

    @Resource
    private RedisTemplate<String, Authority> redisTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        RequestPath path = request.getPath();
        if (path.value().startsWith("doc.html"))
            return chain.filter(exchange);
        else if (path.value().startsWith("search"))
            return chain.filter(exchange);
        HttpCookie cookie = request.getCookies().getFirst("TOKEN");
        String token = cookie == null ? null : cookie.getValue();
        Authority authority = token == null ? null : redisTemplate.opsForValue().get(token);
        if (authority == null)
            authority = new Authority();
        String headerId = "";
        try {
            String primaryUrl = path.subPath(0, 1).value();
            String secondaryUrl = path.elements().size() < 2 ? "" : path.subPath(0, 1).value();
            switch (primaryUrl) {
                case "account" -> {
                    switch (secondaryUrl) {
                        case "register":
                        case "login":
                        case "verify":
                            return chain.filter(exchange);
                        default: {
                            if (authority.getUserId() == null)
                                throw new AcademicException(ExceptionType.UNAUTHORIZED);
                        }
                    }
                }
                case "researcher" -> {
                    if ("certify".equals(secondaryUrl)) {
                        if (authority.getUserId() == null)
                            throw new AcademicException(ExceptionType.UNAUTHORIZED);
                        headerId = authority.getUserId();
                    }
                    else {
                        if (authority.getResearcherId() == null)
                            throw new AcademicException(ExceptionType.UNAUTHORIZED);
                        headerId = authority.getResearcherId();
                    }
                }
                case "admin" -> {
                    if (!"login".equals(secondaryUrl) && !authority.isAdmin())
                        throw new AcademicException(ExceptionType.UNAUTHORIZED);
                    return chain.filter(exchange);
                }
                default -> {
                    return chain.filter(exchange);
                }
            }
        }
        catch (AcademicException exception) {
            JSONObject jsonObject = (JSONObject) JSONObject.toJSON(new Result<Void>().withFailure(exception.getMessage()));
            ServerHttpResponse response = exchange.getResponse();
            DataBuffer buffer = response.bufferFactory().wrap(jsonObject.toJSONString().getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(buffer));
        }
        ServerHttpRequest newRequest = request.mutate()
                .headers(header -> header.remove("Role-ID"))
                .header("Role-ID", headerId).build();
        return chain.filter(exchange.mutate().request(newRequest).build());
    }

    @Override
    public int getOrder() {
        return 0;
    }
    
}
