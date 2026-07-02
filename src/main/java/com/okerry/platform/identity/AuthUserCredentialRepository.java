package com.okerry.platform.identity;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 사용자 비밀번호 인증 정보를 저장하고 사용자 ID로 조회하는 Repository다.
 */
public interface AuthUserCredentialRepository extends JpaRepository<AuthUserCredential, Long> {

    Optional<AuthUserCredential> findByUserId(Long userId);
}
