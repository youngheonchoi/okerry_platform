package com.okerry.platform.serviceaccess;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationException;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationToken;

public class ServiceAccessUnavailableException extends OAuth2AuthorizationCodeRequestAuthenticationException {

    public ServiceAccessUnavailableException(OAuth2AuthorizationCodeRequestAuthenticationToken authentication) {
        super(new OAuth2Error(
                OAuth2ErrorCodes.ACCESS_DENIED,
                "Service access request is unavailable.",
                null), authentication);
    }
}
