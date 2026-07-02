package com.okerry.platform.identity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * SSO 사용자의 식별 정보와 계정 상태를 {@code auth_users} 테이블에 저장하는 Entity다.
 */
@Entity
@Table(name = "auth_users")
public class AuthUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true, length = 36)
    private String publicId;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserStatus status;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    protected AuthUser() {
    }

    public AuthUser(String email, String name, UserStatus status) {
        this.publicId = UUID.randomUUID().toString();
        this.email = Objects.requireNonNull(email);
        this.name = Objects.requireNonNull(name);
        this.status = Objects.requireNonNull(status);
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public Long getId() { return id; }
    public String getPublicId() { return publicId; }
    public String getEmail() { return email; }
    public String getName() { return name; }
    public UserStatus getStatus() { return status; }
    public boolean isEmailVerified() { return emailVerified; }
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }

    public void recordSuccessfulLogin(LocalDateTime loginAt) {
        this.lastLoginAt = Objects.requireNonNull(loginAt);
        this.updatedAt = loginAt;
    }
}
