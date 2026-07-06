package com.okerry.platform.auth;

import com.okerry.platform.identity.AuthUser;
import com.okerry.platform.identity.AuthUserRepository;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 로그인 성공 이벤트를 받아 사용자의 마지막 로그인 시각을 갱신한다.
 */
@Component
@Profile("neon")
public class LoginSuccessListener {

    private static final Logger log = LoggerFactory.getLogger(LoginSuccessListener.class);

    private final AuthUserRepository userRepository;

    public LoginSuccessListener(AuthUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @EventListener
    @Transactional
    public void recordLastLogin(AuthenticationSuccessEvent event) {
        AuthUser user = userRepository.findByEmail(event.getAuthentication().getName())
                .orElse(null);
        if (user != null) {
            user.recordSuccessfulLogin(LocalDateTime.now());
            log.info("User authentication succeeded: userPublicId={}", user.getPublicId());
        }
    }

    @EventListener
    public void recordLoginFailure(AbstractAuthenticationFailureEvent event) {
        log.warn(
                "Authentication failed: reason={}",
                event.getException().getClass().getSimpleName());
    }
}
