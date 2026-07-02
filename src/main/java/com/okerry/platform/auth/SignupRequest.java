package com.okerry.platform.auth;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 회원가입 화면 입력값을 전달하고 서버 검증 규칙을 정의한다.
 */
public class SignupRequest {

    @NotBlank(message = "이름을 입력해주세요.")
    @Size(max = 100, message = "이름은 100자 이하여야 합니다.")
    private String name;

    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "올바른 이메일 형식으로 입력해주세요.")
    @Size(max = 255, message = "이메일은 255자 이하여야 합니다.")
    private String email;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Size(min = 8, max = 64, message = "비밀번호는 8자 이상 64자 이하여야 합니다.")
    private String password;

    @NotBlank(message = "비밀번호 확인을 입력해주세요.")
    private String passwordConfirmation;

    @AssertTrue(message = "비밀번호와 비밀번호 확인이 일치하지 않습니다.")
    public boolean isPasswordConfirmed() {
        return password != null && password.equals(passwordConfirmation);
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = trim(name); }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = trim(email); }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getPasswordConfirmation() { return passwordConfirmation; }
    public void setPasswordConfirmation(String passwordConfirmation) {
        this.passwordConfirmation = passwordConfirmation;
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }
}
