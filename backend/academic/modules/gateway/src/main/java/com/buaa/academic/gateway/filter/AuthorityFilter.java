package com.buaa.academic.gateway.filter;

import com.alibaba.fastjson.JSONObject;
import com.buaa.academic.model.exception.AcademicException;
import com.buaa.academic.model.exception.ExceptionType;
import com.buaa.academic.model.security.Authority;
import com.buaa.academic.model.web.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.PathContainer;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.List;

@Component
public class AuthorityFilter implements GlobalFilter, Ordered {

    private static final Base64.Encoder encoder = Base64.getEncoder();

    private static String adminAuth;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${auth.max-inactive-interval}")
    private long maxInactiveInterval;

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.password}")
    private String adminPassword;

    @PostConstruct
    public void init() {
        if (adminAuth == null) {
            adminAuth = encoder.encodeToString((adminUsername + '@' + adminPassword).getBytes(StandardCharsets.UTF_8));
        }
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        RequestPath path = request.getPath();
        String pathValue = path.value();

        /* Frequently requested non-auth APIs */
        if (pathValue.startsWith("/search") || pathValue.matches("^/analysis/(?!schedule).*"))
            return chain.filter(exchange);

        /* Resource APIs */
        if (pathValue.startsWith("/resource"))
            return chain.filter(exchange);

        /* API docs */
        if (pathValue.matches("^/(?!/).*/v2/api-docs$"))
            return chain.filter(exchange);

        List<PathContainer.Element> urlElements = path.elements();
        if (urlElements.size() < 2) {
            exchange.getResponse().setStatusCode(HttpStatus.NOT_FOUND);
            return exchange.getResponse().setComplete();
        }

        /* Retrieve & validate cookie value */
        HttpCookie cookie = request.getCookies().getFirst("TOKEN");
        String token = cookie == null ? null : cookie.getValue();
        Authority authority = token == null ? null : (Authority) redisTemplate.opsForValue().get(token);
        if (authority != null) {
            /* For requests without admin authority, reset their inactive interval */
            if (!authority.isAdmin()) {
                redisTemplate.expire(token, Duration.ofSeconds(maxInactiveInterval));
            }
        }
        else
            authority = new Authority();

        String authHeader;
        try {
            String primaryUrl = urlElements.get(1).value();
            String secondaryUrl = urlElements.size() < 4 ? "" : urlElements.get(3).value();

            /* Switch url paths */
            switch (primaryUrl) {
                case "account" -> {
                    switch (secondaryUrl) {
                        case "register", "login", "verify", "code" -> {
                            // These requests do not need authority checks here
                            return chain.filter(exchange);
                        }
                        default -> {
                            if (authority.getUserId() == null)
                                throw new AcademicException(ExceptionType.UNAUTHORIZED);
                            authHeader = authority.getUserId();
                        }
                    }
                }
                case "scholar" -> {
                    if ("certify".equals(secondaryUrl) && authority.getUserId() == null) {
                        // These requests do not need researcherId
                        throw new AcademicException(ExceptionType.UNAUTHORIZED);
                    }
                    else if (authority.getResearcherId() == null) {
                        throw new AcademicException(ExceptionType.UNAUTHORIZED);
                    }
                    authHeader = authority.getUserId();
                }
                case "admin" -> {
                    switch (secondaryUrl) {
                        case "login", "logout", "auth" -> {
                            return chain.filter(exchange);
                        }
                        default -> {
                            if (!authority.isAdmin())
                                throw new AcademicException(ExceptionType.UNAUTHORIZED);
                            authHeader = adminAuth;
                        }
                    }
                }
                default -> {
                    return chain.filter(exchange);
                }
            }
        }
        catch (AcademicException exception) {
            /* Re-write response body for unauthorized requests */
            JSONObject jsonObject = (JSONObject) JSONObject.toJSON(new Result<Void>().withFailure(exception.getMessage()));
            ServerHttpResponse response = exchange.getResponse();
            response.getHeaders().add("Content-Type", MediaType.APPLICATION_JSON.toString());
            DataBuffer buffer = response.bufferFactory().wrap(jsonObject.toJSONString().getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(buffer));
        }

        /* Add authority header */
        ServerHttpRequest newRequest = request.mutate()
                .headers(header -> header.remove("Auth"))
                .header("Auth", authHeader).build();
        return chain.filter(exchange.mutate().request(newRequest).build());
    }

    @Override
    public int getOrder() {
        return 0;
    }
    
}
