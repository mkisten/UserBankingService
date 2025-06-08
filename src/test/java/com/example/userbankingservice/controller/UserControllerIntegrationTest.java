package com.example.userbankingservice.controller;

import com.example.userbankingservice.entity.User;
import com.example.userbankingservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UserControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("John Doe");
    }

    @Test
    @WithMockUser(username = "1", authorities = {"USER"})
    void searchUsers_Success() {
        when(userService.searchUsers(anyString(), anyString(), anyString(), any(LocalDate.class), anyInt(), anyInt()))
                .thenReturn(org.springframework.data.domain.Page.empty());
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/users/search?name=John&page=0&size=10", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService).searchUsers("John", null, null, null, 0, 10);
    }

    @Test
    @WithMockUser(username = "1", authorities = {"USER"})
    void updateEmail_Success() {
        doNothing().when(userService).updateEmail(1L, "newemail@example.com");
        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/api/users/emails", "newemail@example.com", Void.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService).updateEmail(1L, "newemail@example.com");
    }

    @Test
    @WithMockUser(username = "1", authorities = {"USER"})
    void updateEmail_EmailExists() {
        doThrow(new RuntimeException("Email уже используется")).when(userService).updateEmail(1L, "existing@example.com");
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/users/emails", "existing@example.com", String.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(userService).updateEmail(1L, "existing@example.com");
    }

    @Test
    void updateEmail_Unauthorized() {
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/users/emails", "newemail@example.com", String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @WithMockUser(username = "1", authorities = {"USER"})
    void deleteEmail_Success() {
        doNothing().when(userService).deleteEmail(1L, "john@example.com");
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>("john@example.com", headers);
        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/users/emails", HttpMethod.DELETE, entity, Void.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService).deleteEmail(1L, "john@example.com");
    }

    @Test
    @WithMockUser(username = "1", authorities = {"USER"})
    void updatePhone_Success() {
        doNothing().when(userService).updatePhone(1L, "+9876543210");
        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/api/users/phones", "+9876543210", Void.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService).updatePhone(1L, "+9876543210");
    }

    @Test
    @WithMockUser(username = "1", authorities = {"USER"})
    void updatePhone_PhoneExists() {
        doThrow(new RuntimeException("Телефон уже используется")).when(userService).updatePhone(1L, "+1234567890");
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/users/phones", "+1234567890", String.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(userService).updatePhone(1L, "+1234567890");
    }

    @Test
    @WithMockUser(username = "1", authorities = {"USER"})
    void deletePhone_Success() {
        doNothing().when(userService).deletePhone(1L, "+1234567890");
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>("+1234567890", headers);
        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/users/phones", HttpMethod.DELETE, entity, Void.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService).deletePhone(1L, "+1234567890");
    }

    @Test
    @WithMockUser(username = "1", authorities = {"USER"})
    void transferMoney_Success() {
        UserController.TransferRequest request = new UserController.TransferRequest();
        request.setToUserId(2L);
        request.setAmount(BigDecimal.valueOf(100.00));
        doNothing().when(userService).transferMoney(1L, 2L, BigDecimal.valueOf(100.00));
        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/api/users/transfers", request, Void.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService).transferMoney(1L, 2L, BigDecimal.valueOf(100.00));
    }

    @Test
    @WithMockUser(username = "1", authorities = {"USER"})
    void transferMoney_InsufficientFunds() {
        UserController.TransferRequest request = new UserController.TransferRequest();
        request.setToUserId(2L);
        request.setAmount(BigDecimal.valueOf(2000.00));
        doThrow(new RuntimeException("Недостаточно средств")).when(userService).transferMoney(1L, 2L, BigDecimal.valueOf(2000.00));
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/users/transfers", request, String.class);
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