package com.spring.blog.service;

import com.spring.blog.common.enums.SocialType;
import com.spring.blog.domain.User;
import com.spring.blog.exception.VerificationException;
import com.spring.blog.repository.UserRepository;
import com.spring.blog.service.sms.MessageService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationService {

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    private final MessageService messageService;

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${spring.mail.username}")
    private String emailFrom;

    /**
     * 해당 번호로 인증번호 전송
     * @param to 전송할 번호
     */
    @Async
    public CompletableFuture<Boolean> sendVerificationCodeBySms(String to) {
        boolean result = true;
        try {
            log.info("SMS 인증번호 전송 시도");

            String verificationCode = generateCode();
            saveVerificationCodeToRedis(to, verificationCode);
            messageService.sendMessage(to, verificationCode);

        } catch (Exception e) {
            result = false;
            log.error(e.getMessage());
        }

        return CompletableFuture.completedFuture(result);
    }

    /**
     * SMS로 받은 인증번호로 인증이 되면 찾은 이메일 반환
     * @return 찾은 이메일
     */
    public String verifyCodeBySms(String requestCode, String phoneNumber) {

        String verificationCode = getAndDelVerificationCodeFromRedis(phoneNumber);

        if (!StringUtils.equals(requestCode, verificationCode)) {
            log.info("이메일 찾기 핸드폰 인증 실패");
            log.info("요청 코드: {}, 인증 코드: {}", requestCode, verificationCode);
            throw new VerificationException("인증 코드가 일치하지 않습니다.");
        }

        User user = userRepository.findByPhoneNumber(phoneNumber).orElseThrow(
                () -> new EntityNotFoundException("not found user from " + phoneNumber));

        return maskEmail(user.getEmail());
    }

    /**
     * 해당 이메일로 인증번호 전송
     * @param to 전송할 이메일
     */
    @Async
    public CompletableFuture<Boolean> sendVerificationCodeByEmail(String to) {

        boolean result = true;

        log.info("Email 인증번호 전송 시도");

        String verificationCode = generateCode();
        saveVerificationCodeToRedis(to, verificationCode);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(emailFrom + "@naver.com");
        message.setTo(to);
        message.setSubject("[Blog] 이메일 인증을 위한 인증번호를 안내 드립니다.");
        message.setText("이메일 인증 화면으로 돌아가 다음 인증번호를 입력하세요.\n\n" + "인증번호 : " + verificationCode);

        try {
            mailSender.send(message);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            result = false;
        }

        return CompletableFuture.completedFuture(result);
    }

    /**
     * 이메일로 받은 인증번호로 인증이 되면 프론트에서 새로운 비밀번호 입력 받는 화면으로 이동
     * @return 인증 여부
     */
    public SocialType verifyCodeByEmail(String requestCode, String email) {

        String verificationCode = getAndDelVerificationCodeFromRedis(email);

        if (!StringUtils.equals(requestCode, verificationCode)) {
            log.info("비밀번호 찾기 이메일 인증 실패");
            log.info("요청 코드: {}, 인증 코드: {}", requestCode, verificationCode);
            throw new VerificationException("인증 코드가 일치하지 않습니다.");
        }

        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new EntityNotFoundException("not found user from " + email));

        return user.getRegistrationId();
    }

    //임의의 6자리 인증번호를 생성
    private String generateCode() {
        SecureRandom random = new SecureRandom();
        return String.format("%06d", random.nextInt(1_000_000));
    }

    //인증시간(5분)동안 인증번호를 레디스에 저장
    private void saveVerificationCodeToRedis(String key, String verificationCode) {
        ValueOperations<String, String> vo = redisTemplate.opsForValue();
        vo.set(key, verificationCode, Duration.ofMinutes(5));
    }

    //사용자가 보낸 인증번호와 비교하기 위해 레디스로부터 값 반환 후 제거
    private String getAndDelVerificationCodeFromRedis(String key) {
        ValueOperations<String, String> vo = redisTemplate.opsForValue();
        String verificationCode = vo.get(key);
        vo.getOperations().delete(key);
        return verificationCode;
    }

    //이메일 중간 부분을 숨긴 후 반환
    private String maskEmail(String email) {
        int atIndex = email.indexOf('@');

        if (atIndex <= 1) {
            return email;
        }

        String localPart = email.substring(0, atIndex);
        String domainPart = email.substring(atIndex);

        String middle = localPart.substring(1, localPart.length() - 1);

        String maskedLocalPart = localPart.charAt(0) + "*".repeat(middle.length()) + localPart.charAt(localPart.length() - 1);

        return maskedLocalPart + domainPart;
    }
}