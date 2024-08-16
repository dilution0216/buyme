package com.example.buyme.user.controller;

import com.example.buyme.dto.JwtAuthenticationResponse;
import com.example.buyme.dto.LoginRequest;
import com.example.buyme.dto.SignupRequest;
import com.example.buyme.entity.User;
import com.example.buyme.exception.DuplicateEmailException;
import com.example.buyme.exception.InvalidEmailFormatException;
import com.example.buyme.exception.InvalidPhoneNumberException;
import com.example.buyme.service.AuthService;
import com.example.buyme.service.UserService;
import com.example.buyme.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody SignupRequest signupRequest) {
        if (userService.emailExists(signupRequest.getUserEmail())) {
            throw new DuplicateEmailException("이미 사용중인 이메일입니다.");
        }

        if (!ValidationUtil.isValidEmail(signupRequest.getUserEmail())) {
            throw new InvalidEmailFormatException("잘못된 이메일 형식입니다");
        }

        if (!ValidationUtil.isValidPhoneNumber(signupRequest.getUserPhoneNumber())) {
            throw new InvalidPhoneNumberException("잘못된 전화번호 형식입니다");
        }

        User user = new User();
        user.setUserName(signupRequest.getUserName());
        user.setUserEmail(signupRequest.getUserEmail());
        user.setUserPassword(signupRequest.getUserPassword());
        user.setUserPhoneNumber(signupRequest.getUserPhoneNumber());
        user.setAddress(signupRequest.getAddress());

        userService.createUser(user);

        log.info("유저 {} 등록이 성공적으로 완료되었습니다", user.getUserEmail());

        return ResponseEntity.ok("사용자가 성공적으로 등록되었습니다. 이메일을 확인해 주세요.");
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        JwtAuthenticationResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(@RequestHeader("Authorization") String token) {
        authService.logout(token);
        return ResponseEntity.ok("로그아웃되었습니다.");
    }

    @PutMapping("/logout-all")
    public ResponseEntity<?> logoutFromAllDevices(@RequestBody String email) {
        User user = userService.findUserByEmail(email);
        authService.logoutFromAllDevices(user);
        return ResponseEntity.ok("모든 기기에서 로그아웃되었습니다.");
    }
}