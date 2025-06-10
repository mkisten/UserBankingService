package com.example.userbankingservice.controller;

import com.example.userbankingservice.entity.User;
import com.example.userbankingservice.service.UserService;
import com.example.userbankingservice.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UserControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    @MockBean
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("John");
    }

    @Test
    @WithMockUser(username = "1", authorities = {"USER"})
    void searchUsers_Success() {
        // Генерируем токен
        String token = "Bearer " + jwtUtil.generateToken(1L);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);

        // Формируем строку запроса с параметрами
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/api/users/search")
                .queryParam("name", "John")
                .queryParam("page", "0")
                .queryParam("size", "10");

        // Создаем HttpEntity только с заголовками (без тела для GET)
        HttpEntity<?> entity = new HttpEntity<>(headers);

        // Выполняем GET-запрос
        ResponseEntity<String> response = restTemplate.exchange(
                builder.toUriString(), // Используем URL с параметрами
                HttpMethod.GET,
                entity,
                String.class
        );

        // Проверяем статус ответа
        assertEquals(HttpStatus.OK, response.getStatusCode());
        // Проверяем, что userService вызван с правильными аргументами
        verify(userService).searchUsers("John", null, null, null, 0, 10);
    }

    @Test
    @WithMockUser(username = "1", authorities = {"USER"})
    void updateEmail_Success() {
        String token = "Bearer " + jwtUtil.generateToken(1L);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<String> entity = new HttpEntity<>("newemail@example.com", headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/users/emails", HttpMethod.PUT, entity, Void.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService).updateEmail(1L, "newemail@example.com");
    }

    @Test
    @WithMockUser(username = "1", authorities = {"USER"})
    void updateEmail_EmailExists() {
        String token = "Bearer " + jwtUtil.generateToken(1L);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<String> entity = new HttpEntity<>("existing@example.com", headers);

        doThrow(new RuntimeException("Email уже используется")).when(userService).updateEmail(1L, "existing@example.com");
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/users/emails", HttpMethod.PUT, entity, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(userService).updateEmail(1L, "existing@example.com");
    }

    @Test
    void updateEmail_Unauthorized() {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>("newemail@example.com", headers);
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/users/emails", HttpMethod.PUT, entity, String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @WithMockUser(username = "1", authorities = {"USER"})
    void deleteEmail_Success() {
        String token = "Bearer " + jwtUtil.generateToken(1L);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<String> entity = new HttpEntity<>("john@example.com", headers);

        doNothing().when(userService).deleteEmail(1L, "john@example.com");
        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/users/emails", HttpMethod.DELETE, entity, Void.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService).deleteEmail(1L, "john@example.com");
    }

    @Test
    @WithMockUser(username = "1", authorities = {"USER"})
    void updatePhone_Success() {
        String token = "Bearer " + jwtUtil.generateToken(1L);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<String> entity = new HttpEntity<>("+9876543210", headers);

        doNothing().when(userService).updatePhone(1L, "+9876543210");
        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/users/phones", HttpMethod.PUT, entity, Void.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService).updatePhone(1L, "+9876543210");
    }

    @Test
    @WithMockUser(username = "1", authorities = {"USER"})
    void updatePhone_PhoneExists() {
        String token = "Bearer " + jwtUtil.generateToken(1L);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<String> entity = new HttpEntity<>("+1234567890", headers);

        doThrow(new RuntimeException("Телефон уже используется")).when(userService).updatePhone(1L, "+1234567890");
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/users/phones", HttpMethod.PUT, entity, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(userService).updatePhone(1L, "+1234567890");
    }

    @Test
    @WithMockUser(username = "1", authorities = {"USER"})
    void deletePhone_Success() {
        String token = "Bearer " + jwtUtil.generateToken(1L);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<String> entity = new HttpEntity<>("+1234567890", headers);

        doNothing().when(userService).deletePhone(1L, "+1234567890");
        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/users/phones", HttpMethod.DELETE, entity, Void.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService).deletePhone(1L, "+1234567890");
    }

    @Test
    @WithMockUser(username = "1", authorities = {"USER"})
    void transferMoney_Success() {
        String token = "Bearer " + jwtUtil.generateToken(1L);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);

        UserController.TransferRequest request = new UserController.TransferRequest();
        request.setToUserId(2L);
        request.setAmount(BigDecimal.valueOf(100.00));
        HttpEntity<UserController.TransferRequest> entity = new HttpEntity<>(request, headers);

        doNothing().when(userService).transferMoney(1L, 2L, BigDecimal.valueOf(100.00));
        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/api/users/transfers", entity, Void.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService).transferMoney(1L, 2L, BigDecimal.valueOf(100.00));
    }

    @Test
    @WithMockUser(username = "1", authorities = {"USER"})
    void transferMoney_InsufficientFunds() {
        String token = "Bearer " + jwtUtil.generateToken(1L);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);

        UserController.TransferRequest request = new UserController.TransferRequest();
        request.setToUserId(2L);
        request.setAmount(BigDecimal.valueOf(2000.00));
        HttpEntity<UserController.TransferRequest> entity = new HttpEntity<>(request, headers);

        doThrow(new RuntimeException("Недостаточно средств")).when(userService).transferMoney(1L, 2L, BigDecimal.valueOf(2000.00));
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/users/transfers", entity, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(userService).transferMoney(1L, 2L, BigDecimal.valueOf(2000.00));
    }

    @Test
    void transferMoney_Unauthorized() {
        UserController.TransferRequest request = new UserController.TransferRequest();
        request.setToUserId(2L);
        request.setAmount(BigDecimal.valueOf(100.00));
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/users/transfers", request, String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}