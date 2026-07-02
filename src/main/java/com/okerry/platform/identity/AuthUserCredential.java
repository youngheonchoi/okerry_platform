package com.okerry.platform.identity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 사용자별 비밀번호 해시와 로그인 실패 및 잠금 정보를
 * {@code auth_user_credentials} 테이블에 저장하는 Entity다.
 */
@Entity
@Table(name = "auth_user_credentials")
public class AuthUserCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private AuthUser user;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "password_algorithm", nullable = false, length = 50)
    private String passwordAlgorithm;

    @Column(name = "failed_login_count", nullable = false)
    private int failedLoginCount;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Column(name = "password_updated_at", nullable = false)
    private LocalDateTime passwordUpdatedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected AuthUserCredential() {
    }

    public AuthUserCredential(AuthUser user, String passwordHash, String passwordAlgorithm) {
        this.user = Objects.requireNonNull(user);
        this.passwordHash = Objects.requireNonNull(passwordHash);
        this.passwordAlgorithm = Objects.requireNonNull(passwordAlgorithm);
        this.passwordUpdatedAt = LocalDateTime.now();
        this.createdAt = this.passwordUpdatedAt;
        this.updatedAt = this.createdAt;
    }

    public Long getId() { return id; }
    public AuthUser getUser() { return user; }
    public String getPasswordHash() { return passwordHash; }
    public String getPasswordAlgorithm() { return passwordAlgorithm; }
}
