package com.example.userbankingservice.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "phone_data")
@Data
public class PhoneData {
    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(length = 13, unique = true)
    private String phone;
}
