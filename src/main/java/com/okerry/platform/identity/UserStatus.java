package com.okerry.platform.identity;

/**
 * SSO 사용자 계정의 현재 인증 가능 상태를 표현한다.
 */
public enum UserStatus {
    ACTIVE,
    PENDING,
    LOCKED,
    DISABLED,
    WITHDRAWN
}
