package com.okerry.platform.auth;

/**
 * 회원가입 데이터 저장 중 무결성 오류가 발생했을 때 내부 상세정보를 감싸서 전달한다.
 */
public class SignupPersistenceException extends RuntimeException {

    public SignupPersistenceException(Throwable cause) {
        super("Could not persist signup data", cause);
    }
}
