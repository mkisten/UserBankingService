package com.example.userbankingservice.service;

import com.example.userbankingservice.entity.User;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public interface UserService {
    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);
    User getUserById(Long id);
    void updateEmail(Long userId, String email);
    void deleteEmail(Long userId, String email);
    void updatePhone(Long userId, String phone);
    void deletePhone(Long userId, String phone);
    Page<User> searchUsers(String name, String email, String phone, LocalDate dateOfBirth, int page, int size);
    void transferMoney(Long fromUserId, Long toUserId, BigDecimal amount);
}