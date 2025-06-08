package com.example.userbankingservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "User Banking Service API",
                description = "API для управления банковскими операциями пользователей, включая аутентификацию, поиск и переводы денег.",
                version = "1.0.0",
                contact = @Contact(
                        name = "Support Team",
                        email = "support@example.com",
                        url = "https://github.com/mkisten/UserBankingService"
                ),
                license = @License(
                        name = "MIT License",
                        url = "https://github.com/mkisten/UserBankingService/blob/main/LICENSE"
                )
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Локальный сервер")
        }
)
public class SwaggerConfig {
}