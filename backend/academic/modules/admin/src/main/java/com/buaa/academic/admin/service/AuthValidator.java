package com.buaa.academic.admin.service;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class AuthValidator {

    private static final Object AUTH_LOCK = new Object();

    private static boolean initialized = false;

    private static String passwordSHA256;

    private static String authHeader;

    @Value("${auth.username}")
    private String username;

    @Value("${auth.password}")
    private String password;

    @PostConstruct
    public void init() {
        synchronized (AUTH_LOCK) {
            if (!initialized) {
                passwordSHA256 = DigestUtils.sha256Hex(password);
                authHeader = Base64.getEncoder().encodeToString((username + '@' + password).getBytes(StandardCharsets.UTF_8));
                initialized = true;
            }
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean passwordCheck(String username, String password) {
        return this.username.equals(username) && passwordSHA256.equals(password);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean headerCheck(String headerAuth) {
        return authHeader.equals(headerAuth);
    }

}
