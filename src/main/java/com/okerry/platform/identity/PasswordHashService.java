package com.okerry.platform.identity;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 비밀번호 원문을 BCrypt로 해시하고 입력 비밀번호와 저장된 해시의 일치 여부를 검증한다.
 */
@Service
public class PasswordHashService {

    public static final String ALGORITHM = "BCRYPT";

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    public String hash(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public boolean matches(String rawPassword, String passwordHash) {
        return passwordEncoder.matches(rawPassword, passwordHash);
    }
}
