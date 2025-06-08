package com.example.userbankingservice.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "email_data")
@Data
public class EmailData {
    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(length = 200, unique = true)
    private String email;
}
