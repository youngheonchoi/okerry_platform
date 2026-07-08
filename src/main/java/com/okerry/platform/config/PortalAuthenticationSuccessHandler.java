package com.okerry.platform.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

/**
 * Restores real client-started requests, but does not restore framework error pages.
 */
public class PortalAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private static final String DEFAULT_TARGET_URL = "/portal/services";

    private final HttpSessionRequestCache requestCache = new HttpSessionRequestCache();

    public PortalAuthenticationSuccessHandler() {
        setDefaultTargetUrl(DEFAULT_TARGET_URL);
        setRequestCache(requestCache);
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws ServletException, IOException {
        SavedRequest savedRequest = requestCache.getRequest(request, response);
        if (savedRequest != null && isErrorRequest(savedRequest.getRedirectUrl())) {
            requestCache.removeRequest(request, response);
        }

        super.onAuthenticationSuccess(request, response, authentication);
    }

    private boolean isErrorRequest(String redirectUrl) {
        if (redirectUrl == null || redirectUrl.isBlank()) {
            return false;
        }
        try {
            return "/error".equals(URI.create(redirectUrl).getPath());
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}
