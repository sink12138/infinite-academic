package com.buaa.academic.account.service;

import com.buaa.academic.account.verifyModel.UserToVerify;
import com.buaa.academic.document.entity.User;
import com.buaa.academic.model.security.Authority;


public interface AccountService {
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

    Authority getAuthorityByToken(String token);

    /**
     * Delete token
     * @param key Key
     */
    void deleteRedisKey(String key);

    /**
     * Get userId from redis for verify
     * @param code Code for verification
     * @return User to verify
     */
    UserToVerify getUserToVerifyByCode(String code);
}
