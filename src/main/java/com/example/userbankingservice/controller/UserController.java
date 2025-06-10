package com.example.userbankingservice.controller;

import com.example.userbankingservice.entity.User;
import com.example.userbankingservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "API для управления пользователями, включая поиск, обновление контактов и переводы денег")
public class UserController {
    private final UserService userService;

    @Operation(summary = "Поиск пользователей по фильтрам", description = "Возвращает страницу пользователей на основе имени, email, телефона или даты рождения")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешно возвращена страница пользователей"),
            @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса")
    })
    @GetMapping("/search")
    public Page<User> searchUsers(
            @Parameter(description = "Имя пользователя для поиска", example = "John") @RequestParam(required = false) String name,
            @Parameter(description = "Email пользователя для поиска", example = "user@example.com") @RequestParam(required = false) String email,
            @Parameter(description = "Телефон пользователя для поиска", example = "+1234567890") @RequestParam(required = false) String phone,
            @Parameter(description = "Дата рождения пользователя для фильтрации", example = "2000-01-01") @RequestParam(required = false) LocalDate dateOfBirth,
            @Parameter(description = "Номер страницы (начиная с 0)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы", example = "10") @RequestParam(defaultValue = "10") int size) {
        return userService.searchUsers(name, email, phone, dateOfBirth, page, size);
    }

    @Operation(summary = "Обновление email пользователя", description = "Добавляет новый email для авторизованного пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email успешно обновлен"),
            @ApiResponse(responseCode = "400", description = "Email уже используется или некорректный запрос"),
            @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    @PutMapping("/emails")
    public void updateEmail(
            @Parameter(description = "ID авторизованного пользователя", required = true, example = "1") @AuthenticationPrincipal Long userId,
            @Parameter(description = "Новый email", required = true, example = "newemail@example.com") @RequestBody String email) {
        userService.updateEmail(userId, email);
    }

    @Operation(summary = "Удаление email пользователя", description = "Удаляет email для авторизованного пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email успешно удален"),
            @ApiResponse(responseCode = "400", description = "Email не найден или удаление последнего email запрещено"),
            @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    @DeleteMapping("/emails")
    public void deleteEmail(
            @Parameter(description = "ID авторизованного пользователя", required = true, example = "1") @AuthenticationPrincipal Long userId,
            @Parameter(description = "Email для удаления", required = true, example = "oldemail@example.com") @RequestBody String email) {
        userService.deleteEmail(userId, email);
    }

    @Operation(summary = "Обновление телефона пользователя", description = "Добавляет новый телефон для авторизованного пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Телефон успешно обновлен"),
            @ApiResponse(responseCode = "400", description = "Телефон уже используется или некорректный запрос"),
            @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    @PutMapping("/phones")
    public void updatePhone(
            @Parameter(description = "ID авторизованного пользователя", required = true, example = "1") @AuthenticationPrincipal Long userId,
            @Parameter(description = "Новый телефон", required = true, example = "+9876543210") @RequestBody String phone) {
        userService.updatePhone(userId, phone);
    }

    @Operation(summary = "Удаление телефона пользователя", description = "Удаляет телефон для авторизованного пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Телефон успешно удален"),
            @ApiResponse(responseCode = "400", description = "Телефон не найден или удаление последнего телефона запрещено"),
            @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    @DeleteMapping("/phones")
    public void deletePhone(
            @Parameter(description = "ID авторизованного пользователя", required = true, example = "1") @AuthenticationPrincipal Long userId,
            @Parameter(description = "Телефон для удаления", required = true, example = "+1234567890") @RequestBody String phone) {
        userService.deletePhone(userId, phone);
    }

    @Operation(summary = "Перевод денег между пользователями", description = "Выполняет перевод денег от авторизованного пользователя к указанному получателю")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Перевод успешно выполнен"),
            @ApiResponse(responseCode = "400", description = "Недостаточно средств или некорректная сумма"),
            @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    @PostMapping("/transfers")
    public void transferMoney(
            @Parameter(description = "ID авторизованного пользователя (отправителя)", required = true, example = "1") @AuthenticationPrincipal Long fromUserId,
            @Parameter(description = "Запрос на перевод", required = true) @RequestBody TransferRequest request) {
        log.info("Переовод от пользователя: {}", fromUserId);
        userService.transferMoney(fromUserId, request.getToUserId(), request.getAmount());
    }

    @Data
    public static class TransferRequest {
        @Parameter(description = "ID получателя", required = true, example = "2")
        private Long toUserId;

        @Parameter(description = "Сумма перевода", required = true, example = "100.00")
        private BigDecimal amount;
    }
}