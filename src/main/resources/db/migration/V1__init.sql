CREATE TABLE "user" (
                        id BIGINT PRIMARY KEY,
                        name VARCHAR(500) NOT NULL,
                        date_of_birth DATE NOT NULL,
                        password VARCHAR(500) NOT NULL
);

CREATE TABLE account (
                         id BIGINT PRIMARY KEY,
                         user_id BIGINT UNIQUE NOT NULL,
                         balance DECIMAL(19,2) NOT NULL CHECK (balance >= 0),
                         initial_balance DECIMAL(19,2) NOT NULL,
                         FOREIGN KEY (user_id) REFERENCES "user"(id)
);

CREATE TABLE email_data (
                            id BIGINT PRIMARY KEY,
                            user_id BIGINT NOT NULL,
                            email VARCHAR(200) UNIQUE NOT NULL,
                            FOREIGN KEY (user_id) REFERENCES "user"(id)
);

CREATE TABLE phone_data (
                            id BIGINT PRIMARY KEY,
                            user_id BIGINT NOT NULL,
                            phone VARCHAR(13) UNIQUE NOT NULL,
                            FOREIGN KEY (user_id) REFERENCES "user"(id)
);

-- Вставка начальных данных
INSERT INTO "user" (id, name, date_of_birth, password) VALUES
                                                           (1, 'Иван Иванов', '1993-05-01', '$2a$10$d9NlFCwNy9EkACLhuY5dRuwuSFcADmt7p9CSUeRaSYhc4wt9tGE4y'),
                                                           (2, 'Мария Смирнова', '1988-10-15', '$2a$10$80rWdkJH/xZAd0.UvHEMxeGodxJLF684Ms6F0FzVWLitpqTEX5Su6');

INSERT INTO account (id, user_id, balance, initial_balance) VALUES
                                                                (1, 1, 1000.00, 1000.00),
                                                                (2, 2, 500.00, 500.00);

INSERT INTO email_data (id, user_id, email) VALUES
                                                (1, 1, 'ivan.ivanov@example.com'),
                                                (2, 2, 'maria.smirnova@example.com');

INSERT INTO phone_data (id, user_id, phone) VALUES
                                                (1, 1, '79201234567'),
                                                (2, 2, '79207654321');