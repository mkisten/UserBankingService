package com.example.userbankingservice.controller;

import com.example.userbankingservice.entity.User;
import com.example.userbankingservice.service.UserService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "API для аутентификации пользователей через email или телефон")
public class AuthController {
    private final UserService userService;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);

        // Убеждаемся что ключ достаточно длинный для HMAC-SHA256 (минимум 32 байта)
        if (keyBytes.length < 32) {
            byte[] paddedKey = new byte[32];
            System.arraycopy(keyBytes, 0, paddedKey, 0, Math.min(keyBytes.length, 32));
            keyBytes = paddedKey;
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Operation(summary = "Аутентификация пользователя", description = "Выполняет вход пользователя по email или телефону с проверкой пароля и возвращает JWT токен")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешная аутентификация, возвращен JWT токен"),
            @ApiResponse(responseCode = "400", description = "Некорректные учетные данные или отсутствуют email/телефон"),
            @ApiResponse(responseCode = "401", description = "Неверный пароль")
    })
    @PostMapping("/login")
    public String login(
            @Parameter(description = "Данные для входа (email или телефон и пароль)", required = true)
            @RequestBody LoginRequest request) {

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

        // Генерация токена с новым API
        String token = Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("userId", user.getId())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey())  // Используем SecretKey вместо строки
                .compact();

        log.info("Токен выдан для пользователя ID: {}", user.getId());
        return token;
    }

    @Data
    public static class LoginRequest {
        @Parameter(description = "Email пользователя для входа", example = "user@example.com")
        private String email;

        @Parameter(description = "Телефон пользователя для входа", example = "+1234567890")
        private String phone;

        @Parameter(description = "Пароль пользователя", example = "password123", required = true)
        private String password;
    }
}