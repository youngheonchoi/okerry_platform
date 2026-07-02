package com.okerry.platform.auth;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** 이메일별 가장 최근 인증 요청을 저장하고 조회한다. */
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

    Optional<EmailVerification> findTopByEmailOrderByCreatedAtDesc(String email);
}
