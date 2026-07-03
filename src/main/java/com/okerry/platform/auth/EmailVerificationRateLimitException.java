package com.okerry.platform.auth;

/** 이메일 인증번호 재전송 대기시간이 지나지 않았을 때 발생한다. */
public class EmailVerificationRateLimitException extends RuntimeException {
}
