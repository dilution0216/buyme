package com.example.buyme.user.repository;

import com.example.buyme.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<com.example.buyme.user.entity.User, Long> {
    Optional<User> findByUserEmail(String email);
    boolean existsByUserEmail(String email);
}
