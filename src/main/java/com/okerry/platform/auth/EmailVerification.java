package com.okerry.platform.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;

/** 회원가입 이메일 인증번호의 해시, 만료 및 확인 시도 상태를 저장한다. */
@Entity
@Table(name = "email_verifications")
public class EmailVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(name = "code_hash", nullable = false, length = 255)
    private String codeHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected EmailVerification() {
    }

    public EmailVerification(String email, String codeHash, LocalDateTime now) {
        this.email = Objects.requireNonNull(email);
        this.codeHash = Objects.requireNonNull(codeHash);
        this.createdAt = Objects.requireNonNull(now);
        this.expiresAt = now.plusMinutes(5);
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getCodeHash() { return codeHash; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public int getAttemptCount() { return attemptCount; }
    public LocalDateTime getVerifiedAt() { return verifiedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public boolean canAttempt(LocalDateTime now) {
        return verifiedAt == null && attemptCount < 5 && expiresAt.isAfter(now);
    }

    public void recordFailure() {
        this.attemptCount++;
    }

    public void markVerified(LocalDateTime now) {
        this.verifiedAt = Objects.requireNonNull(now);
    }
}
