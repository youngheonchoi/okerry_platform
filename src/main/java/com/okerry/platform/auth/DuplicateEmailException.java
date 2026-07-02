package com.okerry.platform.auth;

/**
 * 이미 등록된 이메일로 회원가입을 요청했을 때 발생한다.
 */
public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException() {
        super("Email is already registered");
    }
}
