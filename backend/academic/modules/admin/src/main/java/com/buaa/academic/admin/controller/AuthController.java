package com.buaa.academic.admin.controller;

import com.buaa.academic.model.security.Authority;
import com.buaa.academic.model.web.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.text.RandomStringGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.Duration;

import static org.apache.commons.text.CharacterPredicates.DIGITS;
import static org.apache.commons.text.CharacterPredicates.LETTERS;

@RestController
@Validated
@Api(tags = "身份验证相关")
public class AuthController {

    private static String passwordSHA256;

    private static final RandomStringGenerator generator = new RandomStringGenerator
            .Builder()
            .withinRange('0', 'Z')
            .filteredBy(LETTERS, DIGITS)
            .build();

    @Resource
    private RedisTemplate<String, Authority> redisTemplate;

    @Value("${auth.username}")
    private String username;

    @Value("${auth.password}")
    private String password;

    @Value("${auth.max-valid-interval}")
    private long maxValidInterval;

    @PostConstruct
    public void init() {
        if (passwordSHA256 == null) {
            passwordSHA256 = DigestUtils.sha256Hex(password);
        }
    }

    @PostMapping("/login")
    @ApiOperation(
            value = "管理员登录",
            notes = "这个接口会将请求者置为管理员登录状态，此状态将在30分钟后自动清除。")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", value = "管理员用户名"),
            @ApiImplicitParam(name = "password", value = "管理员密码，注意需要SHA256Hex加密")})
    public Result<Void> login(@CookieValue(name = "TOKEN", required = false) String token,
                              @RequestParam(value = "username") @NotNull @NotEmpty String username,
                              @RequestParam(value = "password") @NotNull @NotEmpty String password,
                              HttpServletResponse response) {
        Result<Void> result = new Result<>();
        if (!this.username.equals(username) || !passwordSHA256.equals(password)) {
            return result.withFailure("用户名或密码错误");
        }
        Authority auth = queryAdminAuth(token);
        if (auth == null) {
            auth = new Authority();
            auth.setAdmin(true);
            if (token == null) {
                token = generator.generate(64);
                Cookie cookie = new Cookie("TOKEN", token);
                cookie.setPath("/");
                cookie.setHttpOnly(true);
                response.addCookie(cookie);
            }
            redisTemplate.opsForValue().set(token, auth);
        }
        redisTemplate.expire(token, Duration.ofSeconds(maxValidInterval));
        return result;
    }

    @PostMapping("/logout")
    @ApiOperation(value = "管理员登出", notes = "此接口会清除用户的管理员登录状态。")
    public Result<Void> logout(@CookieValue(name = "TOKEN", required = false) String token) {
        Result<Void> result = new Result<>();
        Authority auth = queryAdminAuth(token);
        if (auth != null) {
            auth.setAdmin(false);
            redisTemplate.opsForValue().set(token, auth);
        }
        return result;
    }

    private Authority queryAdminAuth(String token) {
        if (token == null)
            return null;
        Authority auth = redisTemplate.opsForValue().get(token);
        if (auth == null || !auth.isAdmin())
            return null;
        return auth;
    }

}
