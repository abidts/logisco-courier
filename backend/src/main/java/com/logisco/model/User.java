package com.logisco.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String email;

    private String phoneNumber;
    private String fullName;

    @Enumerated(EnumType.STRING)
    private AccountType accountType = AccountType.CUSTOMER;

    private String companyName;

    @Enumerated(EnumType.STRING)
    private Role role;

    private boolean active = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Role {
        ADMIN, USER, STAFF
    }

    public enum AccountType {
        CUSTOMER, BUSINESS
    }
}
