package com.okerry.platform.portal;

import com.okerry.platform.identity.AuthUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "service_users")
public class ServiceUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AuthUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private RegisteredService service;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ServiceUserStatus status;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    protected ServiceUser() {
    }

    public ServiceUser(AuthUser user, RegisteredService service, ServiceUserStatus status) {
        this.user = Objects.requireNonNull(user);
        this.service = Objects.requireNonNull(service);
        this.status = Objects.requireNonNull(status);
        this.joinedAt = LocalDateTime.now();
        this.createdAt = this.joinedAt;
        this.updatedAt = this.joinedAt;
    }

    public Long getId() { return id; }
    public AuthUser getUser() { return user; }
    public RegisteredService getService() { return service; }
    public ServiceUserStatus getStatus() { return status; }
    public LocalDateTime getDeletedAt() { return deletedAt; }

    public void delete(LocalDateTime deletedAt) {
        this.deletedAt = Objects.requireNonNull(deletedAt);
        this.updatedAt = deletedAt;
    }
}
