package com.swp.pizzashop.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

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

    @NotBlank
    @Size(max = 50)
    @Column(name = "firstname", length = 50, nullable = false)
    private String firstname;

    @NotBlank
    @Size(max = 50)
    @Column(name = "lastname", length = 50, nullable = false)
    private String lastname;

    @NotBlank
    @Email
    @Size(max = 100)
    @Column(name = "email", length = 100, nullable = false, unique = true)
    private String email;

    @NotBlank
    @Size(max = 255)
    @Column(name = "password", length = 255, nullable = false)
    private String password;

    @Size(max = 20)
    @Column(name = "phone", length = 20)
    private String phone;

    @Size(max = 20)
    @Column(name = "status", length = 20)
    private String status = "ACTIVE"; // matches DEFAULT 'ACTIVE'

    @Size(max = 30)
    @Column(name = "provider", length = 30)
    private String provider;

    @Size(max = 100)
    @Column(name = "provider_id", length = 100)
    private String providerId;

    // Inline email verification fields
    @Size(max = 128)
    @Column(name = "verification_token", length = 128)
    private String verificationToken;

    @Column(name = "verification_expires_at")
    private LocalDateTime verificationExpiresAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

}