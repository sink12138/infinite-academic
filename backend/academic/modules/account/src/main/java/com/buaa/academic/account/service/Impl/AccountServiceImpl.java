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

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void sendVerifyEmail(User user, String action) {
        new VerifyEmailThread(user, action).run();
    }

    @Override
    public String addAuthority(User user) {
        String token = generateRandomCode(64);
        Authority authority = new Authority(user.getId(), user.getResearcherId(), false);
        redisTemplate.opsForValue().set(token, authority);
        redisTemplate.expire(token, Duration.ofDays(1));
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

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String FROM_ADDRESS;

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
            if (!action.equals("找回密码")) {
                code = generateRandomCode(16);
            } else {
                code = generateRandomCode(5);
            }
            redisTemplate.opsForValue().set(code, user);
            redisTemplate.expire(code, Duration.ofHours(1));

            if (!action.equals("找回密码")) {
                Date date = new Date();

                MimeMessage mimeMessage = javaMailSender.createMimeMessage();

                // set content
                Context context = new Context();
                context.setVariable("action", action);
                context.setVariable("username", user.getUsername());
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
                context.setVariable("createTime", simpleDateFormat.format(date));

                // set check link
                String checkLink = "http://121.36.98.60:8090/account/verify?code=" + code;
                context.setVariable("checkLink", checkLink);
                String process = templateEngine.process("CheckEmail.html", context);

                // basic setting
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
                helper.setSubject("Infinite Academic | %s邮箱验证".formatted(action));
                helper.setFrom(FROM_ADDRESS);
                helper.setTo(user.getEmail());
                helper.setSentDate(date);
                helper.setText(process, true);

                javaMailSender.send(mimeMessage);
            } else {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(user.getEmail());
                message.setFrom(FROM_ADDRESS);
                message.setSubject(action);
                message.setText(code);
                javaMailSender.send(message);
            }
        }
    }
}
