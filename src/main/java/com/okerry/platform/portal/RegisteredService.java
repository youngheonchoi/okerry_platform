package com.okerry.platform.portal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "registered_services")
public class RegisteredService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_key", nullable = false, unique = true, length = 100)
    private String serviceKey;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(name = "home_url", nullable = false, length = 500)
    private String homeUrl;

    @Column(name = "login_start_url", nullable = false, length = 500)
    private String loginStartUrl;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "oauth2_client_id", nullable = false, unique = true, length = 100)
    private String oauth2ClientId;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected RegisteredService() {
    }

    public RegisteredService(
            String serviceKey,
            String name,
            String description,
            String homeUrl,
            String loginStartUrl,
            String logoUrl,
            String oauth2ClientId,
            int displayOrder,
            boolean enabled) {
        this.serviceKey = Objects.requireNonNull(serviceKey);
        this.name = Objects.requireNonNull(name);
        this.description = description;
        this.homeUrl = Objects.requireNonNull(homeUrl);
        this.loginStartUrl = Objects.requireNonNull(loginStartUrl);
        this.logoUrl = logoUrl;
        this.oauth2ClientId = Objects.requireNonNull(oauth2ClientId);
        this.displayOrder = displayOrder;
        this.enabled = enabled;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public Long getId() { return id; }
    public String getServiceKey() { return serviceKey; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getHomeUrl() { return homeUrl; }
    public String getLoginStartUrl() { return loginStartUrl; }
    public String getLogoUrl() { return logoUrl; }
    public String getOauth2ClientId() { return oauth2ClientId; }
    public int getDisplayOrder() { return displayOrder; }
    public boolean isEnabled() { return enabled; }
}
