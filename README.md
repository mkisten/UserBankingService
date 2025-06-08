# User Banking Service

Это Spring Boot приложение для управления банковскими операциями пользователей, включая управление пользователями, обновление email/телефона, переводы денег и аутентификацию через JWT.

## Обзор

Приложение предоставляет RESTful API для операций, связанных с пользователями, и включает следующие функции:
- Операции CRUD с пользователями (поиск, обновление email/телефона, удаление email/телефона).
- Перевод денег между счетами пользователей.
- Аутентификация на основе JWT с использованием email/телефона и пароля.
- Безопасное хранение паролей с использованием BCryptPasswordEncoder.
- Сервисный слой, организованный через интерфейсы для модульности и тестоспособности.

## Структура проекта

```
user-banking-service/
├── pom.xml
├── README.md
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/userbankingservice/
│   │   │       ├── Application.java
│   │   │       ├── entity/
│   │   │       │   ├── User.java
│   │   │       │   ├── Account.java
│   │   │       │   ├── EmailData.java
│   │   │       │   └── PhoneData.java
│   │   │       ├── repository/
│   │   │       │   ├── UserRepository.java
│   │   │       │   ├── AccountRepository.java
│   │   │       │   ├── EmailDataRepository.java
│   │   │       │   └── PhoneDataRepository.java
│   │   │       ├── service/
│   │   │       │   ├── UserService.java (интерфейс)
│   │   │       │   ├── UserServiceImpl.java
│   │   │       │   ├── BalanceUpdateScheduler.java (интерфейс)
│   │   │       │   └── BalanceUpdateSchedulerImpl.java
│   │   │       ├── controller/
│   │   │       │   ├── UserController.java
│   │   │       │   └── AuthController.java
│   │   │       ├── security/
│   │   │       │   ├── JwtAuthenticationFilter.java
│   │   │       │   ├── UserDetailsServiceImpl.java
│   │   │       │   └── SecurityConfig.java
│   │   │       └── config/
│   │   │           └── SwaggerConfig.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/migration/
│   │           └── V1__init.sql
│   └── test/
│       ├── java/
│       │   └── com/example/userbankingservice/
│       │       ├── service/
│       │       │   └── UserServiceTest.java
│       │       └── controller/
│       │           └── UserControllerIntegrationTest.java
│       └── resources/
│           └── application-test.yml
```

## Требования

- Java 17 или выше
- Maven 3.6+
- Docker (для Testcontainers в тестах)
- PostgreSQL (опционально, для локального запуска без Docker)

## Установка

1. Склонируйте репозиторий:
   ```bash
   git clone https://github.com/mkisten/UserBankingService.git
   cd user-banking-service
   ```

2. Установите зависимости:
   ```bash
   mvn clean install
   ```

3. Настройте базу данных (опционально, если не используете Docker):
   - Обновите `src/main/resources/application.yml` с вашими учетными данными PostgreSQL:
     ```yaml
     spring:
       datasource:
         url: jdbc:postgresql://localhost:5432/user_banking
         username: your_username
         password: your_password
       jpa:
         hibernate:
           ddl-auto: update
     jwt:
       secret: your-secure-secret-key
       expiration: 86400000
     ```

4. Сборка и запуск приложения:
   ```bash
   mvn spring-boot:run
   ```

## Запуск тестов

Для запуска тестов (включая интеграционные тесты с Testcontainers):
```bash
mvn test
```

Убедитесь, что Docker запущен, так как Testcontainers создаст временный экземпляр PostgreSQL для тестирования.

## Документация API

API задокументировано с использованием Swagger. Доступ к ней возможен по адресу `http://localhost:8080/swagger-ui.html` после запуска приложения.

### Аутентификация
- **Эндпоинт**: `POST /api/auth/login`
- **Тело запроса**:
  ```json
  {
    "email": "user@example.com",
    "password": "password123"
  }
  ```
  или
  ```json
  {
    "phone": "+1234567890",
    "password": "password123"
  }
  ```
- **Ответ**: JWT токен (например, `eyJhbGciOiJIUzUxMiJ9...`).

Используйте токен в заголовке `Authorization` с префиксом `Bearer ` для защищенных эндпоинтов.

### Операции с пользователями
- **Поиск пользователей**: `GET /api/users/search?name=John&email=user@example.com&page=0&size=10`
- **Обновление email**: `PUT /api/users/emails` (требуется заголовок Authorization)
  - Тело: `"newemail@example.com"`
- **Удаление email**: `DELETE /api/users/emails` (требуется заголовок Authorization)
  - Тело: `"oldemail@example.com"`
- **Обновление телефона**: `PUT /api/users/phones` (требуется заголовок Authorization)
  - Тело: `"+9876543210"`
- **Удаление телефона**: `DELETE /api/users/phones` (требуется заголовок Authorization)
  - Тело: `"+1234567890"`
- **Перевод денег**: `POST /api/users/transfers` (требуется заголовок Authorization)
  - Тело:
    ```json
    {
      "toUserId": 2,
      "amount": "100.00"
    }
    ```

## Безопасность
- Аутентификация осуществляется через JWT токены.
- Пароли хешируются с использованием `BCryptPasswordEncoder`.
- Защита CSRF отключена, сессии являются бесстатусными.

## Конфигурация
- **Секретный ключ JWT**: Настраивается через `jwt.secret` в `application.yml`.
- **Срок действия JWT**: Настраивается через `jwt.expiration` в `application.yml` (в миллисекундах).
- **База данных**: Использует Flyway для миграций (см. `db/migration/V1__init.sql`).

## Участие в разработке
1. Сделайте форк репозитория.
2. Создайте ветку для новой функции (`git checkout -b feature/new-feature`).
3. Зафиксируйте изменения (`git commit -m "Добавлена новая функция"`).
4. Отправьте изменения в ветку (`git push origin feature/new-feature`).
5. Откройте Pull Request.

## Лицензия
Этот проект лицензирован под лицензией MIT - см. файл [LICENSE](LICENSE) для деталей.