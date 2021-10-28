package com.buaa.academic.account.controller;

import com.buaa.academic.account.repository.AccountRepository;
import com.buaa.academic.account.service.AccountService;
import com.buaa.academic.document.entity.User;
import com.buaa.academic.model.security.Authority;
import com.buaa.academic.model.web.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@RestController
public class AccountController {
    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @PostMapping("/register")
    public Result<Void> register(@RequestParam(value = "email") String email,
                                   @RequestParam(value = "username") String username,
                                   @RequestParam(value = "password") String password) {
        if (accountService.exam_password(password)) {
            User original_user = accountRepository.findUserByEmail(email);
            if (original_user != null) {
                return new Result<Void>().withFailure("该邮箱已注册");
            }
            User new_user = new User(email, username, password);
            accountRepository.save(new_user);
            accountService.sendVerifyEmail(new_user, "注册");
            return new Result<>();
        }
        return new Result<Void>().withFailure("密码不合法或强度过低");
    }

    @Autowired
    private HttpServletRequest request;

    @GetMapping("/verify")
    public ModelAndView verify(@RequestParam(value = "token") String token) {
        Map<String, String> userMap = accountService.getUserMapByToken(token);
        if (userMap == null) {
            return new ModelAndView("CheckFailure");
        } else {
            String userId = userMap.get("userId");
            User user = accountRepository.findUserById(userId);
            if (user.isVerified()) {
                user.setEmail(userMap.get("email"));
            } else {
                user.setVerified(true);
            }
            accountRepository.save(user);
            accountService.deleteToken(token);
            request.setAttribute("email", user.getEmail());
            return new ModelAndView("CheckSuccess");
        }
    }

    private String getToken() {
        String token = null;
        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals("TOKEN")) {
                token = cookie.getValue();
                break;
            }
        }
        return token;
    }

    @Autowired
    private HttpServletResponse response;

    private void setToken(String token) {
        Cookie cookie = new Cookie("TOKEN", token);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }

    @PostMapping("/login")
    public Result<Void> login(@RequestParam(value = "email") String email,
                              @RequestParam(value = "password") String password) {
        String token = getToken();
        if (token != null) {
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

    @PostMapping("/logout")
    public Result<Void> logout() {
        String token = getToken();
        accountService.deleteToken(token);
        return new Result<>();
    }

    private User getUserByToken() {
        String token = getToken();
        Authority authority = accountService.getAuthority(token);
        String userId = authority.getUserId();
        return accountRepository.findUserById(userId);
    }

    @GetMapping("/profile")
    public Result<User> profile() {
        User user = getUserByToken();
        return new Result<User>().withData(user);
    }

    @PostMapping("/profile/modify/info")
    public Result<Void> modifyInfo(@RequestParam(value = "username") String username,
                               @RequestParam(value = "password") String password) {
        if (!accountService.exam_password(password)) {
            return new Result<Void>().withFailure("密码不合法或强度过低");
        }
        User user = getUserByToken();
        user.setUsername(username);
        user.setPassword(password);
        accountRepository.save(user);
        return new Result<>();
    }

    @PostMapping("/profile/modify/email")
    public Result<Void> modifyEmail(@RequestParam(value = "email") String email) {
        User user = getUserByToken();
        user.setEmail(email);
        accountService.sendVerifyEmail(user, "修改");
        return new Result<>();
    }
}
