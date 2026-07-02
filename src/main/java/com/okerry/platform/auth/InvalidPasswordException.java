package com.okerry.platform.auth;

/**
 * 비밀번호가 해시 알고리즘의 안전한 입력 제한을 초과했을 때 발생한다.
 */
public class InvalidPasswordException extends RuntimeException {

    public InvalidPasswordException() {
        super("Password exceeds the supported encoded length");
    }
}
