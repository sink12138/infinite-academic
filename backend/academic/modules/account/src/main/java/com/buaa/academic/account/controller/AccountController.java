package com.buaa.academic.account.controller;

import com.buaa.academic.account.repository.AccountRepository;
import com.buaa.academic.account.service.AccountService;
import com.buaa.academic.document.entity.User;
import com.buaa.academic.model.web.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import springfox.documentation.annotations.ApiIgnore;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Objects;

@RestController
@Validated
@Api(tags = "用户接口", value = "/account")
public class AccountController {
    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;


    @ApiOperation(value = "注册接口")
    @PostMapping("/register")
    public Result<Void> register(@RequestParam(value = "email") @NotNull @Email String email,
                                 @RequestParam(value = "username") @NotNull @NotEmpty @Length(max = 10) String username,
                                 @RequestParam(value = "password") @NotNull String password) {
        User original_user = accountRepository.findUserByEmail(email);
        if (original_user != null) {
            return new Result<Void>().withFailure("该邮箱已注册");
        }
        original_user = new User(email, username, password);
        accountService.sendVerifyEmail(original_user, "注册");
        return new Result<>();
    }

    @Autowired
    private HttpServletRequest request;

    @ApiIgnore
    @GetMapping("/verify")
    public ModelAndView verify(@RequestParam(value = "code") String code) {
        User userToVerify = accountService.getUserByCode(code);
        if (userToVerify == null) {
            return new ModelAndView("CheckFailure");
        } else {
            User user = accountRepository.findUserByEmail(userToVerify.getEmail());
            if (user != null) {
                return new ModelAndView("CheckFailure");
            }
            accountRepository.save(userToVerify);
            accountService.deleteRedisKey(code);
            request.setAttribute("email", userToVerify.getEmail());
            return new ModelAndView("CheckSuccess");
        }
    }

    @Autowired
    private HttpServletResponse response;

    private void setToken(String token) {
        Cookie cookie = new Cookie("TOKEN", token);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }

    @ApiOperation(value = "登录")
    @PostMapping("/login")
    public Result<Void> login(@CookieValue(name = "TOKEN", required = false) String token,
                              @RequestParam(value = "email") @Email  @NotNull String email,
                              @RequestParam(value = "password") @NotNull String password) {
        if (token != null && accountService.getAuthorityByToken(token) != null) {
            return new Result<Void>().withFailure("当前账户已登录");
        }
        User user = accountRepository.findUserByEmail(email);
        if (!user.getPassword().equals(password)) {
            return new Result<Void>().withFailure("密码错误");
        }
        token = accountService.addAuthority(user);
        setToken(token);
        return new Result<>();
    }

    @ApiOperation(value = "登出")
    @PostMapping("/logout")
    public Result<Void> logout(@CookieValue(name = "TOKEN", required = false) String token) {
        accountService.deleteRedisKey(token);
        return new Result<>();
    }

    @ApiOperation(value = "获取用户信息，包括用户id、用户名、邮箱、密码、学者id")
    @GetMapping("/profile")
    public Result<User> profile(@RequestHeader(value = "Role-ID") String userId) {
        User user = accountRepository.findUserById(userId);
        return new Result<User>().withData(user);
    }

    @ApiOperation(value = "修改用户信息，可修改用户名和密码")
    @PostMapping("/profile/modify/info")
    public Result<Void> modifyInfo( @RequestHeader(value = "Role-ID") @NotNull String userId,
                                    @RequestParam(value = "username") @NotNull @NotEmpty @Length(max = 10)  String username,
                                    @RequestParam(value = "password") @NotNull String password) {
        User user = accountRepository.findUserById(userId);
        user.setUsername(username);
        user.setPassword(password);
        accountRepository.save(user);
        return new Result<>();
    }

    @ApiOperation(value = "修改邮箱")
    @PostMapping("/profile/modify/email")
    public Result<Void> modifyEmail(@RequestHeader(value = "Role-ID") @NotNull String userId,
                                    @RequestParam(value = "email") @Email @NotNull String email) {
        User user = accountRepository.findUserById(userId);
        if (Objects.equals(email, user.getEmail())){
            return new Result<Void>().withFailure("不可修改为当前邮箱");
        }
        User original_user = accountRepository.findUserByEmail(email);
        if (original_user != null) {
            return new Result<Void>().withFailure("该邮箱已注册");
        }
        user.setEmail(email);
        accountService.sendVerifyEmail(user, "修改");
        return new Result<>();
    }

    @ApiOperation(value = "找回密码获取验证码", notes = "向用户邮箱发送验证码")
    @PostMapping("/forget/apply")
    public Result<Void> forgetApply(@RequestParam(value = "email") @Email String email) {
        User tmpUser = new User();
        tmpUser.setEmail(email);
        accountService.sendVerifyEmail(tmpUser, "找回密码");
        return new Result<>();
    }

    @ApiOperation(value = "找回密码设置新密码", notes = "用户输入新密码和验证码")
    @PostMapping("/forget/submit")
    public Result<Void> forgetSubmit(@RequestParam(value = "code") String code,
                                     @RequestParam(value = "newPassword") @NotNull String newPassword) {
        User userToVerify = accountService.getUserByCode(code);
        if (userToVerify == null) {
            return new Result<Void>().withFailure("验证码已失效");
        }
        User user = accountRepository.findUserByEmail(userToVerify.getEmail());
        if (user == null) {
            return new Result<Void>().withFailure("该邮箱未注册");
        }
        user.setPassword(newPassword);
        accountService.deleteRedisKey(code);
        accountRepository.save(user);
        return new Result<>();
    }

    @ApiOperation(value = "用于测试跨域")
    @GetMapping("/echo")
    public String echo(@RequestParam(value = "str") String str) {
        return str;
    }
}
