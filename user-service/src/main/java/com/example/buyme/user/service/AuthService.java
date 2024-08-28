package com.example.buyme.user.service;

import com.example.buyme.user.config.JwtTokenProvider;
import com.example.buyme.user.dto.JwtAuthenticationResponse;
import com.example.buyme.user.dto.LoginRequest;
import com.example.buyme.user.entity.User;
import com.example.buyme.user.exception.EmailNotVerifiedException;
import com.example.buyme.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public JwtAuthenticationResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByUserEmail(loginRequest.getUserEmail())
                .orElseThrow(() -> new EmailNotVerifiedException("잘못된 이메일 또는 비밀번호입니다"));

        if (!passwordEncoder.matches(loginRequest.getUserPassword(), user.getUserPassword())) {
            throw new EmailNotVerifiedException("잘못된 이메일 또는 비밀번호입니다");
        }

        if (!user.isEmailVerified()) {
            throw new EmailNotVerifiedException("이메일이 검증되지 않았습니다");
        }

        String jwt = tokenProvider.generateToken(user);
        log.info("유저 {} 가 로그인되었습니다.", user.getUserEmail());
        return new JwtAuthenticationResponse(jwt);
    }

    public void logout(String token) {
        tokenProvider.invalidateToken(token);
        log.info("로그아웃 되었습니다.");
    }

    public void logoutFromAllDevices(User user) {
        tokenProvider.invalidateAllTokensForUser(user);
        log.info("유저 {} 가 모든 장치에서 로그아웃되었습니다.", user.getUserEmail());
    }
}