package com.okerry.platform.serviceaccess;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationException;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

@Component
@Profile("neon")
public class ServiceAccessAuthorizationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {
        if (exception instanceof ServiceAccessDeniedException accessDeniedException) {
            PendingServiceAccessRequest pendingRequest = accessDeniedException.getPendingRequest();
            request.getSession(true).setAttribute(pendingRequest.sessionAttributeName(), pendingRequest);
            response.sendRedirect("/service-access/request?request_id=" + pendingRequest.requestId());
            return;
        }
        if (exception instanceof ServiceAccessUnavailableException) {
            response.sendRedirect("/service-access/unavailable");
            return;
        }
        if (exception instanceof OAuth2AuthorizationCodeRequestAuthenticationException authorizationException) {
            sendOAuth2ErrorResponse(response, authorizationException);
            return;
        }
        response.sendError(HttpStatus.BAD_REQUEST.value());
    }

    private void sendOAuth2ErrorResponse(
            HttpServletResponse response,
            OAuth2AuthorizationCodeRequestAuthenticationException exception) throws IOException {
        OAuth2Error error = exception.getError();
        OAuth2AuthorizationCodeRequestAuthenticationToken authentication =
                exception.getAuthorizationCodeRequestAuthentication();
        if (authentication == null || !StringUtils.hasText(authentication.getRedirectUri())) {
            response.sendError(HttpStatus.BAD_REQUEST.value(), error.toString());
            return;
        }

        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString(authentication.getRedirectUri())
                .queryParam(OAuth2ParameterNames.ERROR, error.getErrorCode());
        if (StringUtils.hasText(error.getDescription())) {
            uriBuilder.queryParam(OAuth2ParameterNames.ERROR_DESCRIPTION,
                    UriUtils.encode(error.getDescription(), StandardCharsets.UTF_8));
        }
        if (StringUtils.hasText(error.getUri())) {
            uriBuilder.queryParam(OAuth2ParameterNames.ERROR_URI,
                    UriUtils.encode(error.getUri(), StandardCharsets.UTF_8));
        }
        if (StringUtils.hasText(authentication.getState())) {
            uriBuilder.queryParam(OAuth2ParameterNames.STATE,
                    UriUtils.encode(authentication.getState(), StandardCharsets.UTF_8));
        }
        response.sendRedirect(uriBuilder.build(true).toUriString());
    }
}
