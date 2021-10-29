package com.buaa.academic.account.service.Impl;

import com.buaa.academic.account.service.AccountService;
import com.buaa.academic.document.entity.User;
import com.buaa.academic.model.security.Authority;
import lombok.SneakyThrows;
import org.apache.commons.text.RandomStringGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import javax.annotation.Resource;
import javax.mail.internet.MimeMessage;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.text.CharacterPredicates.DIGITS;
import static org.apache.commons.text.CharacterPredicates.LETTERS;

@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Boolean exam_password(String password) {
        if (password.length() > 20 || password.length() < 6) {
            return false;
        }
        boolean containCharacter = false;
        boolean containNumber = false;
        for (char character : password.toCharArray()) {
            if (character >= '0' && character <= '9') {
                containNumber = true;
            } else if ((character >= 'a' && character <= 'z') || (character >= 'A' && character <= 'Z') ) {
                containCharacter = true;
            } else {
                return false;
            }
        }
        return containCharacter && containNumber;
    }

    @Override
    public void sendVerifyEmail(User user, String action) {
        new VerifyEmailThread(user.getId(), user.getEmail(), user.getUsername(), action).run();
    }

    @Override
    public String addAuthority(User user) {
        String token = generateToken(64);
        Authority authority = new Authority(user.getId(), user.getResearcherId(), false);
        redisTemplate.opsForValue().set(token, authority);
        redisTemplate.expire(token, Duration.ofDays(1));
        return token;
    }

    @Override
    public Authority getAuthority(String token) {
        return (Authority) redisTemplate.opsForValue().get(token);
    }

    @Override
    public void deleteToken(String token) {
        redisTemplate.delete(token);
    }

    @Override
    public Map<String, String> getUserMapByToken(String token) {
        Map<String, String> userMap = new HashMap<>();
        userMap.put("userId", (String) redisTemplate.opsForHash().get(token, "userId"));
        userMap.put("email", (String) redisTemplate.opsForHash().get(token, "email"));
        return userMap;
    }

    private String generateToken(int length) {
        RandomStringGenerator randomStringGenerator = new RandomStringGenerator.Builder()
                .withinRange('0', 'Z')
                .filteredBy(LETTERS, DIGITS)
                .build();
        return randomStringGenerator.generate(length);
    }

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String FROM_ADDRESS;

    @Value("${settings.test.host}")
    private String IP_ADDRESS;

    @Value("${server.port}")
    private String PORT;

    private class VerifyEmailThread implements Runnable {
        String userId;
        String email;
        String username;
        String action;

        public VerifyEmailThread(String userId, String email, String username, String action) {
            this.userId = userId;
            this.email = email;
            this.username = username;
            this.action = action;
        }

        @SneakyThrows
        @Override
        public void run() {
            String token = generateToken(16);
            redisTemplate.opsForHash().put(token, "userId", userId);
            redisTemplate.opsForHash().put(token, "email", email);
            redisTemplate.expire(token, Duration.ofMinutes(10));

            Date date = new Date();

            MimeMessage mimeMessage = javaMailSender.createMimeMessage();

            Context context = new Context();
            context.setVariable("action", action);
            context.setVariable("checkLink",  IP_ADDRESS + PORT +"/verify?token=" + token);
            context.setVariable("username", username);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
            context.setVariable("createTime", simpleDateFormat.format(date));
            String process = templateEngine.process("CheckEmail.html", context);

            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setSubject("Infinite Academic | 注册邮箱验证");
            helper.setFrom(FROM_ADDRESS);
            helper.setTo(email);
            helper.setSentDate(date);
            helper.setText(process, true);

            javaMailSender.send(mimeMessage);
        }
    }
}
