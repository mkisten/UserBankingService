package com.example.userbankingservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "\"user\"")
@Data
@ToString(exclude = "account")
public class User {
    @Id
    private Long id;

    @Column(length = 500)
    private String name;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(length = 500)
    private String password;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Account account;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<EmailData> emails;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<PhoneData> phones;
}
