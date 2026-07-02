package com.okerry.platform.auth;

import com.okerry.platform.identity.AuthUser;
import com.okerry.platform.identity.AuthUserCredential;
import com.okerry.platform.identity.AuthUserCredentialRepository;
import com.okerry.platform.identity.AuthUserRepository;
import com.okerry.platform.identity.AuthUserRoleRepository;
import com.okerry.platform.identity.PasswordHashService;
import com.okerry.platform.identity.UserStatus;
import java.time.LocalDateTime;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * DB의 사용자, 비밀번호 해시, 권한과 계정 상태를 Spring Security 인증 정보로 변환한다.
 */
@Service
@Profile("db")
public class DatabaseUserDetailsService implements UserDetailsService {

    private final AuthUserRepository userRepository;
    private final AuthUserCredentialRepository credentialRepository;
    private final AuthUserRoleRepository userRoleRepository;

    public DatabaseUserDetailsService(
            AuthUserRepository userRepository,
            AuthUserCredentialRepository credentialRepository,
            AuthUserRoleRepository userRoleRepository) {
        this.userRepository = userRepository;
        this.credentialRepository = credentialRepository;
        this.userRoleRepository = userRoleRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String email = SignupService.normalizeEmail(username);
        AuthUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));
        AuthUserCredential credential = credentialRepository.findByUserId(user.getId())
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));
        if (!PasswordHashService.ALGORITHM.equals(credential.getPasswordAlgorithm())) {
            throw new UsernameNotFoundException("Invalid credentials");
        }
        String[] authorities = userRoleRepository.findRoleCodesByUserId(user.getId()).stream()
                .toArray(String[]::new);
        if (authorities.length == 0) {
            throw new UsernameNotFoundException("Invalid credentials");
        }

        boolean lockedByTime = credential.getLockedUntil() != null
                && credential.getLockedUntil().isAfter(LocalDateTime.now());
        boolean accountLocked = user.getStatus() == UserStatus.LOCKED || lockedByTime;
        boolean disabled = user.getStatus() != UserStatus.ACTIVE && !accountLocked;

        return User.withUsername(user.getEmail())
                .password(credential.getPasswordHash())
                .authorities(authorities)
                .accountLocked(accountLocked)
                .disabled(disabled)
                .build();
    }
}
