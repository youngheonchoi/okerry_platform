package com.okerry.platform.auth;

import com.okerry.platform.identity.AuthUser;
import com.okerry.platform.identity.AuthUserRepository;
import java.time.LocalDateTime;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 로그인 성공 이벤트를 받아 사용자의 마지막 로그인 시각을 갱신한다.
 */
@Component
@Profile("neon")
public class LoginSuccessListener {

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
        }
    }
}
