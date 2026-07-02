package com.okerry.platform.identity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 사용자와 권한의 다대다 관계를 {@code auth_user_roles} 테이블에 저장하는 매핑 Entity다.
 */
@Entity
@Table(name = "auth_user_roles", uniqueConstraints = @UniqueConstraint(
        name = "uk_auth_user_roles_user_role", columnNames = {"user_id", "role_id"}))
public class AuthUserRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AuthUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private AuthRole role;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected AuthUserRole() {
    }

    public AuthUserRole(AuthUser user, AuthRole role) {
        this.user = Objects.requireNonNull(user);
        this.role = Objects.requireNonNull(role);
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public AuthUser getUser() { return user; }
    public AuthRole getRole() { return role; }
}
