package com.okerry.platform.identity;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 사용자와 권한의 연결 관계를 저장하고 사용자별 권한을 조회하는 Repository다.
 */
public interface AuthUserRoleRepository extends JpaRepository<AuthUserRole, Long> {

    List<AuthUserRole> findAllByUserId(Long userId);

    boolean existsByUserIdAndRoleId(Long userId, Long roleId);
}
