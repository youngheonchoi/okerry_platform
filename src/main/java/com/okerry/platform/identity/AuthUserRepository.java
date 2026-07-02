package com.okerry.platform.identity;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * SSO 사용자를 저장하고 이메일 또는 외부 공개 식별자로 조회하는 Repository다.
 */
public interface AuthUserRepository extends JpaRepository<AuthUser, Long> {

    Optional<AuthUser> findByEmail(String email);

    Optional<AuthUser> findByPublicId(String publicId);

    boolean existsByEmail(String email);
}
