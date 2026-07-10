package com.okerry.platform.serviceaccess;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

public record PendingServiceAccessRequest(
        String requestId,
        Long userId,
        String userEmail,
        String userName,
        Long serviceId,
        String serviceName,
        String serviceKey,
        String oauth2ClientId,
        LocalDateTime requestedAt) implements Serializable {

    public static final String SESSION_ATTRIBUTE_PREFIX =
            PendingServiceAccessRequest.class.getName() + ".";

    public PendingServiceAccessRequest {
        Objects.requireNonNull(requestId);
        Objects.requireNonNull(userId);
        Objects.requireNonNull(userEmail);
        Objects.requireNonNull(userName);
        Objects.requireNonNull(serviceId);
        Objects.requireNonNull(serviceName);
        Objects.requireNonNull(serviceKey);
        Objects.requireNonNull(oauth2ClientId);
        Objects.requireNonNull(requestedAt);
    }

    public String sessionAttributeName() {
        return SESSION_ATTRIBUTE_PREFIX + requestId;
    }
}
