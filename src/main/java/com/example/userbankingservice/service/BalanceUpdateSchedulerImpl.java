package com.example.userbankingservice.service;

import com.example.userbankingservice.entity.Account;
import com.example.userbankingservice.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BalanceUpdateSchedulerImpl implements BalanceUpdateScheduler {
    private static final Logger logger = LoggerFactory.getLogger(BalanceUpdateSchedulerImpl.class);
    private final AccountRepository accountRepository;

    // Статический флаг для отключения шедулера
    private static volatile boolean isSchedulerDisabled = false;

    @Override
    @Scheduled(fixedRate = 30000) // Каждые 30 секунд
    @Transactional
    public void updateBalances() {
        // Проверка флага отключения
        if (isSchedulerDisabled) {
            logger.info("Шедулер отключен, так как все счета достигли максимального баланса");
            return;
        }

        logger.info("Запуск обновления балансов");
        List<Account> accounts = accountRepository.findAll();
        boolean allMaxedOut = true; // Флаг для проверки, достигли ли все счета максимума

        for (Account account : accounts) {
            BigDecimal maxBalance = account.getInitialBalance().multiply(new BigDecimal("2.07"));
            BigDecimal newBalance = account.getBalance().multiply(new BigDecimal("1.10"));
            if (newBalance.compareTo(maxBalance) > 0) {
                newBalance = maxBalance;
                logger.info("Достигнут максимальный баланс для счета ID: {}. Установлен баланс: {}", account.getId(), newBalance);
            } else {
                allMaxedOut = false; // Если хотя бы один счет не достиг максимума, сбрасываем флаг
            }
            account.setBalance(newBalance);
            accountRepository.save(account);
            logger.debug("Баланс счета ID: {} обновлен до {}", account.getId(), newBalance);
        }

        // Если все счета достигли максимального баланса, отключаем шедулер
        if (allMaxedOut) {
            isSchedulerDisabled = true;
            logger.info("Все счета достигли максимального баланса. Шедулер отключен.");
        }

        logger.info("Обновление балансов завершено");
    }
}