package com.buaa.academic.account.service.Impl;

import com.buaa.academic.account.service.AccountService;
import com.buaa.academic.document.entity.User;
import com.buaa.academic.model.security.Authority;
import lombok.SneakyThrows;
import org.apache.commons.text.RandomStringGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
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
import static org.apache.commons.text.CharacterPredicates.DIGITS;
import static org.apache.commons.text.CharacterPredicates.LETTERS;

@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${auth.max-inactive-interval}")
    private long maxInactiveInterval;

    @Override
    public void sendVerifyEmail(User user, String action) {
        new Thread(new VerifyEmailThread(user, action)).start();
    }

    @Override
    public String addAuthority(User user) {
        String token = generateRandomCode(64);
        Authority authority = new Authority(user.getId(), user.getResearcherId(), false);
        redisTemplate.opsForValue().set(token, authority);
        redisTemplate.expire(token, Duration.ofSeconds(maxInactiveInterval));
        return token;
    }

    @Override
    public Authority getAuthorityByToken(String token) {
        return (Authority) redisTemplate.opsForValue().get(token);
    }

    @Override
    public void deleteRedisKey(String key) {
        redisTemplate.delete(key);
    }

    @Override
    public User getUserByCode(String code) {
        return (User) redisTemplate.opsForValue().get(code);
    }

    private String generateRandomCode(int length) {
        RandomStringGenerator randomStringGenerator = new RandomStringGenerator.Builder()
                .withinRange('0', 'Z')
                .filteredBy(LETTERS, DIGITS)
                .build();
        return randomStringGenerator.generate(length);
    }

    @Resource
    private JavaMailSender javaMailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String FROM_ADDRESS;

    @Value("${spring.mail.verify.host}")
    private String VERIFY_HOST;

    private class VerifyEmailThread implements Runnable {
        User user;
        String action;

        public VerifyEmailThread(User user, String action) {
            this.user = user;
            this.action = action;
        }

        @SneakyThrows
        @Override
        public void run() {
            String code;
            if (!action.equals("????????????") && !action.equals("????????????")) {
                code = generateRandomCode(16);
            } else {
                code = generateRandomCode(5);
            }
            redisTemplate.opsForValue().set(code, user);
            redisTemplate.expire(code, Duration.ofHours(1));

            if (!action.equals("????????????") && !action.equals("????????????")) {
                Date date = new Date();

                MimeMessage mimeMessage = javaMailSender.createMimeMessage();

                // set content
                Context context = new Context();
                context.setVariable("action", action);
                context.setVariable("username", user.getUsername());
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy???MM???dd??? HH:mm:ss");
                context.setVariable("createTime", simpleDateFormat.format(date));

                // set check link
                String checkLink = VERIFY_HOST + "/verify?code=" + code;
                context.setVariable("checkLink", checkLink);
                String process = templateEngine.process("CheckEmail.html", context);

                // basic setting
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
                helper.setSubject("Infinite Academic | %s????????????".formatted(action));
                helper.setFrom(FROM_ADDRESS);
                helper.setTo(user.getEmail());
                helper.setSentDate(date);
                helper.setText(process, true);

                javaMailSender.send(mimeMessage);
            } else {
                MimeMessage mimeMessage = javaMailSender.createMimeMessage();

                // set content
                Context context = new Context();
                context.setVariable("action", action);
                context.setVariable("code", code);
                String process = templateEngine.process("Code.html", context);

                // basic setting
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
                Date date = new Date();
                helper.setSubject("Infinite Academic | %s????????????".formatted(action));
                helper.setFrom(FROM_ADDRESS);
                helper.setTo(user.getEmail());
                helper.setSentDate(date);
                helper.setText(process, true);

                javaMailSender.send(mimeMessage);
            }
        }
    }
}
