package com.okerry.platform.auth;

/** 이메일 인증 API의 사용자용 처리 결과다. */
public record EmailVerificationResponse(boolean success, String message) {
}
