package com.example.userbankingservice.controller;

import com.example.userbankingservice.entity.User;
import com.example.userbankingservice.service.UserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/search")
    public Page<User> searchUsers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) LocalDate dateOfBirth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return userService.searchUsers(name, email, phone, dateOfBirth, page, size);
    }

    @PutMapping("/emails")
    public void updateEmail(@AuthenticationPrincipal Long userId, @RequestBody String email) {
        userService.updateEmail(userId, email);
    }

    @DeleteMapping("/emails")
    public void deleteEmail(@AuthenticationPrincipal Long userId, @RequestBody String email) {
        userService.deleteEmail(userId, email);
    }

    @PutMapping("/phones")
    public void updatePhone(@AuthenticationPrincipal Long userId, @RequestBody String phone) {
        userService.updatePhone(userId, phone);
    }

    @DeleteMapping("/phones")
    public void deletePhone(@AuthenticationPrincipal Long userId, @RequestBody String phone) {
        userService.deletePhone(userId, phone);
    }

    @PostMapping("/transfers")
    public void transferMoney(@AuthenticationPrincipal Long fromUserId, @RequestBody TransferRequest request) {
        userService.transferMoney(fromUserId, request.getToUserId(), request.getAmount());
    }

    @Data
    public static class TransferRequest {
        private Long toUserId;
        private BigDecimal amount;
//
//        public Long getToUserId() { return toUserId; }
//        public void setToUserId(Long toUserId) { this.toUserId = toUserId; }
//        public BigDecimal getAmount() { return amount; }
//        public void setAmount(BigDecimal amount) { this.amount = amount; }
    }
}
