package com.example.userbankingservice.controller;

import com.example.userbankingservice.entity.User;
import com.example.userbankingservice.service.UserService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final BCryptPasswordEncoder passwordEncoder;
    @Value("${jwt.secret}")
    private String jwtSecret;
    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request) {
        // Поиск пользователя по email или phone
        User user = null;
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            user = userService.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        } else if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            user = userService.findByPhone(request.getPhone())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        } else {
            log.error("Не указаны email или phone");
            throw new RuntimeException("Укажите email или телефон");
        }

        // Проверка пароля
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.error("Неверные учетные данные для email: {} или phone: {}", request.getEmail(), request.getPhone());
            throw new RuntimeException("Неверные учетные данные");
        }

        // Генерация токена
        String token = Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .claim("userId", user.getId())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
        log.info("Токен выдан для пользователя ID: {}", user.getId());
        return token;
    }

    @Data
    public static class LoginRequest {
        private String email;
        private String phone;
        private String password;
    }
}