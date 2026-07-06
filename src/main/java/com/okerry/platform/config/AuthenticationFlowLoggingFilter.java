package com.okerry.platform.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 인증 흐름의 단계와 결과만 기록한다. Query string, header, body는 인증정보가
 * 포함될 수 있으므로 로그에 기록하지 않는다.
 */
@Component
@Profile("neon")
public class AuthenticationFlowLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationFlowLoggingFilter.class);
    private static final Set<String> LOGGED_PATHS = Set.of(
            "/login",
            "/logout",
            "/oauth2/authorize",
            "/oauth2/token",
            "/oauth2/revoke",
            "/oauth2/jwks",
            "/userinfo",
            "/.well-known/openid-configuration");

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !LOGGED_PATHS.contains(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        long startedAt = System.nanoTime();

        log.info(
                "Authentication flow request started: requestId={}, method={}, path={}",
                requestId,
                request.getMethod(),
                request.getRequestURI());

        try {
            filterChain.doFilter(request, response);
        } finally {
            long elapsedMillis = (System.nanoTime() - startedAt) / 1_000_000;
            log.info(
                    "Authentication flow request completed: requestId={}, method={}, path={}, status={}, elapsedMs={}",
                    requestId,
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    elapsedMillis);
        }
    }
}
