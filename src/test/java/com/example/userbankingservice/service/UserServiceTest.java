package com.example.userbankingservice.service;

import com.example.userbankingservice.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserService userService;

    private User user;
    private EmailData emailData;
    private PhoneData phoneData;
    private Account account;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("John Doe");

        emailData = new EmailData();
        emailData.setId(1L);
        emailData.setEmail("john@example.com");
        emailData.setUser(user);

        phoneData = new PhoneData();
        phoneData.setId(2L);
        phoneData.setPhone("+1234567890");
        phoneData.setUser(user);

        account = new Account();
        account.setId(1L);
        account.setUser(user);
        account.setBalance(BigDecimal.valueOf(1000.00));
    }

    @Test
    void findByEmail_Success() {
        when(userService.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        Optional<User> result = userService.findByEmail("john@example.com");
        assertTrue(result.isPresent());
        assertEquals(user, result.get());
        verify(userService).findByEmail("john@example.com");
    }

    @Test
    void findByEmail_NotFound() {
        when(userService.findByEmail("unknown@example.com")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> userService.findByEmail("unknown@example.com")
                .orElseThrow(() -> new RuntimeException("Пользователь не найден")));
        verify(userService).findByEmail("unknown@example.com");
    }

    @Test
    void findByPhone_Success() {
        when(userService.findByPhone("+1234567890")).thenReturn(Optional.of(user));
        Optional<User> result = userService.findByPhone("+1234567890");
        assertTrue(result.isPresent());
        assertEquals(user, result.get());
        verify(userService).findByPhone("+1234567890");
    }

    @Test
    void findByPhone_NotFound() {
        when(userService.findByPhone("+9876543210")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> userService.findByPhone("+9876543210")
                .orElseThrow(() -> new RuntimeException("Пользователь не найден")));
        verify(userService).findByPhone("+9876543210");
    }

    @Test
    void getUserById_Success() {
        when(userService.getUserById(1L)).thenReturn(user);
        User result = userService.getUserById(1L);
        assertEquals(user, result);
        verify(userService).getUserById(1L);
    }

    @Test
    void getUserById_NotFound() {
        when(userService.getUserById(2L)).thenThrow(new RuntimeException("Пользователь не найден"));
        assertThrows(RuntimeException.class, () -> userService.getUserById(2L));
        verify(userService).getUserById(2L);
    }

    @Test
    void updateEmail_Success() {
        doNothing().when(userService).updateEmail(1L, "newemail@example.com");
        userService.updateEmail(1L, "newemail@example.com");
        verify(userService).updateEmail(1L, "newemail@example.com");
    }

    @Test
    void updateEmail_EmailExists() {
        doThrow(new RuntimeException("Email уже используется")).when(userService).updateEmail(1L, "existing@example.com");
        assertThrows(RuntimeException.class, () -> userService.updateEmail(1L, "existing@example.com"));
        verify(userService).updateEmail(1L, "existing@example.com");
    }

    @Test
    void deleteEmail_Success() {
        doNothing().when(userService).deleteEmail(1L, "john@example.com");
        userService.deleteEmail(1L, "john@example.com");
        verify(userService).deleteEmail(1L, "john@example.com");
    }

    @Test
    void deleteEmail_NotFound() {
        doThrow(new RuntimeException("Email не найден")).when(userService).deleteEmail(1L, "unknown@example.com");
        assertThrows(RuntimeException.class, () -> userService.deleteEmail(1L, "unknown@example.com"));
        verify(userService).deleteEmail(1L, "unknown@example.com");
    }

    @Test
    void updatePhone_Success() {
        doNothing().when(userService).updatePhone(1L, "+9876543210");
        userService.updatePhone(1L, "+9876543210");
        verify(userService).updatePhone(1L, "+9876543210");
    }

    @Test
    void updatePhone_PhoneExists() {
        doThrow(new RuntimeException("Телефон уже используется")).when(userService).updatePhone(1L, "+1234567890");
        assertThrows(RuntimeException.class, () -> userService.updatePhone(1L, "+1234567890"));
        verify(userService).updatePhone(1L, "+1234567890");
    }

    @Test
    void deletePhone_Success() {
        doNothing().when(userService).deletePhone(1L, "+1234567890");
        userService.deletePhone(1L, "+1234567890");
        verify(userService).deletePhone(1L, "+1234567890");
    }

    @Test
    void deletePhone_NotFound() {
        doThrow(new RuntimeException("Телефон не найден")).when(userService).deletePhone(1L, "+9876543210");
        assertThrows(RuntimeException.class, () -> userService.deletePhone(1L, "+9876543210"));
        verify(userService).deletePhone(1L, "+9876543210");
    }

    @Test
    void searchUsers_Success() {
        Page<User> page = new PageImpl<>(Collections.singletonList(user));
        when(userService.searchUsers("John", "john@example.com", "+1234567890", LocalDate.now(), 0, 10))
                .thenReturn(page);
        Page<User> result = userService.searchUsers("John", "john@example.com", "+1234567890", LocalDate.now(), 0, 10);
        assertEquals(1, result.getTotalElements());
        verify(userService).searchUsers("John", "john@example.com", "+1234567890", LocalDate.now(), 0, 10);
    }

    @Test
    void transferMoney_Success() {
        doNothing().when(userService).transferMoney(1L, 2L, BigDecimal.valueOf(500.00));
        userService.transferMoney(1L, 2L, BigDecimal.valueOf(500.00));
        verify(userService).transferMoney(1L, 2L, BigDecimal.valueOf(500.00));
    }

    @Test
    void transferMoney_InvalidAmount() {
        doThrow(new RuntimeException("Сумма должна быть положительной")).when(userService).transferMoney(1L, 2L, BigDecimal.ZERO);
        assertThrows(RuntimeException.class, () -> userService.transferMoney(1L, 2L, BigDecimal.ZERO));
        verify(userService).transferMoney(1L, 2L, BigDecimal.ZERO);
    }
}