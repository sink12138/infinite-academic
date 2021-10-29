package com.buaa.academic.account.service;

import com.buaa.academic.document.entity.User;
import com.buaa.academic.model.security.Authority;
import org.springframework.stereotype.Service;

import java.util.Map;


public interface AccountService {
    /**
     * Used to exam password,
     * a valid password should contain both number and character,
     * length >= 6
     * @return true if valid, else false
     */
    Boolean exam_password(String password);

    /**
     * Send verify email
     * @param user Target user
     * @param action "注册" or "修改"
     */
    void sendVerifyEmail(User user, String action);

    /**
     * Add authority to redis for current user
     * @param user Instance of current user
     * @return Token generated
     */
    String addAuthority(User user);

    /**
     * Get authority from redis for current user
     * @param token Token from cookie
     * @return Authority Instance of current user
     */
    Authority getAuthority(String token);

    /**
     * Delete token
     * @param token Token
     */
    void deleteToken(String token);

    /**
     * Get userId from redis for verify
     * @param token Token from cookie
     * @return User information map
     */
    Map<String, String> getUserMapByToken(String token);
}
