package com.example.buyme.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    // 로컬 환경에서는 http://localhost:8080, 운영 환경에서는 실제 도메인을 사용
    public void sendVerificationEmail(String to, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("이메일 확인");
        message.setText("이메일을 확인하려면 아래 링크를 클릭하세요.:\n" +
                "http://localhost:8080/verify?token=" + token);
        mailSender.send(message);
        log.info(" {} 로 인증 이메일이 전송되었습니다. ", to);
    }
}