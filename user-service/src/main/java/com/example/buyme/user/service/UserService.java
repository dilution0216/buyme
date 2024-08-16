package com.example.buyme.user.service;

import com.example.buyme.entity.User;
import com.example.buyme.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User createUser(User user) {
        user.setUserPassword(passwordEncoder.encode(user.getUserPassword()));
        return userRepository.save(user);
    }

    public boolean emailExists(String email) {
        return userRepository.existsByUserEmail(email);
    }

    public User findUserByEmail(String email) {
        return userRepository.findByUserEmail(email).orElseThrow(() -> new UsernameNotFoundException("이메일을 통해 사용자를 찾을 수 없습니다.: " + email));
    }
}
