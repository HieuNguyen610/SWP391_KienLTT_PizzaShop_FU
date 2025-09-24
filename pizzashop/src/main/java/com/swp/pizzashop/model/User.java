package com.swp.pizzashop.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstname;
    private String lastname;
    private String email;
    private String password;
    private String phone;
    private String status;
    private String provider;

    @Column(name = "provider_id")
    private String providerId;

    // Inline email verification fields
    @Column(name = "verification_token")
    private String verificationToken;

    @Column(name = "verification_expires_at")
    private LocalDateTime verificationExpiresAt;
}