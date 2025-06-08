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

    @Override
    @Scheduled(fixedRate = 30000) // Каждые 30 секунд
    @Transactional
    public void updateBalances() {
        logger.info("Запуск обновления балансов");
        List<Account> accounts = accountRepository.findAll();
        for (Account account : accounts) {
            BigDecimal maxBalance = account.getInitialBalance().multiply(new BigDecimal("2.07"));
            BigDecimal newBalance = account.getBalance().multiply(new BigDecimal("1.10"));
            if (newBalance.compareTo(maxBalance) > 0) {
                newBalance = maxBalance;
            }
            account.setBalance(newBalance);
            accountRepository.save(account);
            logger.debug("Баланс счета ID: {} обновлен до {}", account.getId(), newBalance);
        }
        logger.info("Обновление балансов завершено");
    }
}
