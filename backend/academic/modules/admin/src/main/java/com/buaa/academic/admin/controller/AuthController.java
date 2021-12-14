package com.buaa.academic.admin.controller;

import com.buaa.academic.admin.service.AuthValidator;
import com.buaa.academic.model.security.Authority;
import com.buaa.academic.model.web.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.text.RandomStringGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.Duration;

import static org.apache.commons.text.CharacterPredicates.DIGITS;
import static org.apache.commons.text.CharacterPredicates.LETTERS;

@RestController
@Validated
@Api(tags = "身份验证相关")
public class AuthController {

    private static final RandomStringGenerator generator = new RandomStringGenerator
            .Builder()
            .withinRange('0', 'Z')
            .filteredBy(LETTERS, DIGITS)
            .build();

    @Autowired
    private AuthValidator authValidator;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${auth.max-valid-interval}")
    private long maxValidInterval;

    @PostMapping("/login")
    @ApiOperation(
            value = "管理员登录",
            notes = "这个接口会将请求者置为管理员登录状态，此状态将在30分钟后自动清除。")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", value = "管理员用户名"),
            @ApiImplicitParam(name = "password", value = "管理员密码，注意需要SHA256Hex加密")})
    public Result<Void> login(@CookieValue(name = "TOKEN", required = false) String token,
                              @RequestParam(value = "username") @NotNull @NotBlank String username,
                              @RequestParam(value = "password") @NotNull @NotBlank String password,
                              HttpServletResponse response) {
        Result<Void> result = new Result<>();
        if (!authValidator.passwordCheck(username, password)) {
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
    @ApiOperation(value = "管理员登出", notes = "此接口会立即清除用户的管理员登录状态。")
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
        Authority auth = (Authority) redisTemplate.opsForValue().get(token);
        if (auth == null || !auth.isAdmin())
            return null;
        return auth;
    }

    @PostMapping("/auth")
    @ApiOperation(
            value = "管理员权限验证",
            notes = "这个接口仅验证传入的管理员用户名和密码是否匹配，不会刷新管理员登录状态的倒计时。</br>" +
                    "可以用于在执行一些敏感操作时，要求二次验证用户名和密码。")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", value = "管理员用户名"),
            @ApiImplicitParam(name = "password", value = "管理员密码，注意需要SHA256Hex加密")})
    public Result<Void> auth(@RequestParam(value = "username") @NotNull @NotBlank String username,
                             @RequestParam(value = "password") @NotNull @NotBlank String password) {
        Result<Void> result = new Result<>();
        if (!authValidator.passwordCheck(username, password)) {
            return result.withFailure("用户名或密码错误");
        }
        return result;
    }

}
