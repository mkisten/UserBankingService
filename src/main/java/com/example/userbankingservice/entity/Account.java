package com.example.userbankingservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

@Entity
@Table(name = "account")
@Data
@ToString(exclude = "user")
public class Account {
    @Id
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column(precision = 19, scale = 2)
    private BigDecimal balance;

    @Column(name = "initial_balance", precision = 19, scale = 2)
    private BigDecimal initialBalance;
}
