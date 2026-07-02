package com.okerry.platform.auth;

/**
 * 회원가입에 필요한 기본 권한이 DB에 준비되지 않았을 때 발생한다.
 */
public class RequiredRoleNotFoundException extends RuntimeException {

    public RequiredRoleNotFoundException(String roleCode) {
        super("Required role is not configured: " + roleCode);
    }
}
