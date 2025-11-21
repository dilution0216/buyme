package com.example.buyme.user.service;


import com.example.buyme.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final com.example.buyme.user.repository.UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 사용자 생성 (캐시 저장)
    @CachePut(value = "users", key = "#result.userId")
    public User createUser(User user) {
        user.setUserPassword(passwordEncoder.encode(user.getUserPassword()));
        return userRepository.save(user);
    }

    // 사용자 ID로 조회 (캐싱)
    @Cacheable(value = "users", key = "#userId")
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다.: ID " + userId));
    }

    // 이메일로 사용자 조회 (캐싱)
    @Cacheable(value = "users", key = "'email:' + #email")
    public User findUserByEmail(String email) {
        return userRepository.findByUserEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("이메일을 통해 사용자를 찾을 수 없습니다.: " + email));
    }

    // 이메일 존재 여부 확인
    public boolean emailExists(String email) {
        return userRepository.existsByUserEmail(email);
    }

    // 사용자 정보 업데이트 (캐시 갱신)
    @CachePut(value = "users", key = "#userId")
    public User updateUser(Long userId, User updatedUser) {
        User user = getUserById(userId);
        user.setUserName(updatedUser.getUserName());
        user.setUserPhoneNumber(updatedUser.getUserPhoneNumber());
        user.setAddress(updatedUser.getAddress());
        return userRepository.save(user);
    }

    // 사용자 삭제 (캐시 무효화)
    @CacheEvict(value = "users", key = "#userId")
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }
}
