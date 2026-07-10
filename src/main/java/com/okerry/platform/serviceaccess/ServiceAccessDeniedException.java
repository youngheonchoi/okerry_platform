package com.okerry.platform.serviceaccess;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationException;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationToken;

public class ServiceAccessDeniedException extends OAuth2AuthorizationCodeRequestAuthenticationException {

    private final PendingServiceAccessRequest pendingRequest;

    public ServiceAccessDeniedException(
            PendingServiceAccessRequest pendingRequest,
            OAuth2AuthorizationCodeRequestAuthenticationToken authentication) {
        super(new OAuth2Error(
                OAuth2ErrorCodes.ACCESS_DENIED,
                "Service access is not allowed for this user.",
                null), authentication);
        this.pendingRequest = pendingRequest;
    }

    public PendingServiceAccessRequest getPendingRequest() {
        return pendingRequest;
    }
}
