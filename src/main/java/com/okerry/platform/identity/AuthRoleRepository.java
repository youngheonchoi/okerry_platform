package com.okerry.platform.identity;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 권한 정보를 저장하고 고유 권한 코드로 조회하는 Repository다.
 */
public interface AuthRoleRepository extends JpaRepository<AuthRole, Long> {

    Optional<AuthRole> findByCode(String code);
}
