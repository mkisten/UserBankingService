package com.example.userbankingservice.service;

import com.example.userbankingservice.entity.*;
import com.example.userbankingservice.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final EmailDataRepository emailDataRepository;
    private final PhoneDataRepository phoneDataRepository;

    @Override
    @Cacheable(value = "users", key = "#email")
    public Optional<User> findByEmail(String email) {
        logger.debug("Поиск пользователя по email: {}", email);
        return emailDataRepository.findByEmail(email)
                .map(EmailData::getUser);
    }

    @Override
    @Cacheable(value = "users", key = "#phone")
    public Optional<User> findByPhone(String phone) {
        logger.debug("Поиск пользователя по телефону: {}", phone);
        return phoneDataRepository.findByPhone(phone)
                .map(PhoneData::getUser);
    }

    @Override
    @Cacheable(value = "users", key = "#id")
    public User getUserById(Long id) {
        logger.info("Получение пользователя с ID: {}", id);
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }

    @Override
    @CacheEvict(value = "users", key = "#userId")
    @Transactional
    public void updateEmail(Long userId, String email) {
        logger.debug("Обновление email для пользователя ID: {}, новый email: {}", userId, email);
        if (emailDataRepository.existsByEmail(email)) {
            logger.error("Email {} уже используется", email);
            throw new RuntimeException("Email уже используется");
        }
        EmailData emailData = new EmailData();
        emailData.setEmail(email);
        emailData.setUser(getUserById(userId));
        emailData.setId(generateId());
        emailDataRepository.save(emailData);
        logger.info("Email {} успешно добавлен для пользователя ID: {}", email, userId);
    }

    @Override
    @CacheEvict(value = "users", key = "#userId")
    @Transactional
    public void deleteEmail(Long userId, String email) {
        logger.debug("Удаление email {} для пользователя ID: {}", email, userId);
        EmailData emailData = emailDataRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email не найден"));
        if (!emailData.getUser().getId().equals(userId)) {
            logger.error("Несанкционированная попытка удаления email {} пользователем ID: {}", email, userId);
            throw new RuntimeException("Несанкционировано");
        }
        if (emailDataRepository.countByUserId(userId) <= 1) {
            logger.error("Нельзя удалить последний email для пользователя ID: {}", userId);
            throw new RuntimeException("Требуется хотя бы один email");
        }
        emailDataRepository.delete(emailData);
        logger.info("Email {} успешно удален для пользователя ID: {}", email, userId);
    }

    @Override
    @CacheEvict(value = "users", key = "#userId")
    @Transactional
    public void updatePhone(Long userId, String phone) {
        logger.debug("Обновление телефона для пользователя ID: {}, новый телефон: {}", userId, phone);
        if (phoneDataRepository.existsByPhone(phone)) {
            logger.error("Телефон {} уже используется", phone);
            throw new RuntimeException("Телефон уже используется");
        }
        PhoneData phoneData = new PhoneData();
        phoneData.setPhone(phone);
        phoneData.setUser(getUserById(userId));
        phoneData.setId(generateId());
        phoneDataRepository.save(phoneData);
        logger.info("Телефон {} успешно добавлен для пользователя ID: {}", phone, userId);
    }

    @Override
    @CacheEvict(value = "users", key = "#userId")
    @Transactional
    public void deletePhone(Long userId, String phone) {
        logger.debug("Удаление телефона {} для пользователя ID: {}", phone, userId);
        PhoneData phoneData = phoneDataRepository.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("Телефон не найден"));
        if (!phoneData.getUser().getId().equals(userId)) {
            logger.error("Несанкционированная попытка удаления телефона {} пользователем ID: {}", phone, userId);
            throw new RuntimeException("Несанкционировано");
        }
        if (phoneDataRepository.countByUserId(userId) <= 1) {
            logger.error("Нельзя удалить последний телефон для пользователя ID: {}", userId);
            throw new RuntimeException("Требуется хотя бы один телефон");
        }
        phoneDataRepository.delete(phoneData);
        logger.info("Телефон {} успешно удален для пользователя ID: {}", phone, userId);
    }

    @Override
    @Cacheable(value = "userSearch", key = "{#name, #email, #phone, #dateOfBirth, #page, #size}")
    public Page<User> searchUsers(String name, String email, String phone, LocalDate dateOfBirth, int page, int size) {
        logger.info("Поиск пользователей с параметрами: name={}, email={}, phone={}, dateOfBirth={}, page={}, size={}",
                name, email, phone, dateOfBirth, page, size);
        Specification<User> spec = Specification.where(null);
        if (name != null) {
            spec = spec.and((root, query, cb) -> cb.like(root.get("name"), name + "%"));
        }
        if (email != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.join("emails").get("email"), email));
        }
        if (phone != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.join("phones").get("phone"), phone));
        }
        if (dateOfBirth != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThan(root.get("dateOfBirth"), dateOfBirth));
        }
        Page<User> result = userRepository.findAll(spec, PageRequest.of(page, size));
        logger.info("Найдено {} пользователей", result.getTotalElements());
        return result;
    }

    @Override
    @Transactional
    public void transferMoney(Long fromUserId, Long toUserId, BigDecimal amount) {
        logger.info("Перевод денег от пользователя ID: {} к пользователю ID: {}, сумма: {}", fromUserId, toUserId, amount);
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            logger.error("Сумма перевода должна быть положительной: {}", amount);
            throw new RuntimeException("Сумма должна быть положительной");
        }
        Account fromAccount = accountRepository.findByUserIdForUpdate(fromUserId);
        Account toAccount = accountRepository.findByUserIdForUpdate(toUserId);
        if (fromAccount == null || toAccount == null) {
            logger.error("Счет отправителя или получателя не найден");
            throw new RuntimeException("Счет не найден");
        }
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            logger.error("Недостаточно средств на счете пользователя ID: {}", fromUserId);
            throw new RuntimeException("Недостаточно средств");
        }
        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);
        logger.info("Перевод успешно выполнен: {} от ID: {} к ID: {}", amount, fromUserId, toUserId);
    }

    private Long generateId() {
        return System.currentTimeMillis();
    }
}