package com.okerry.platform.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** 이메일 인증번호 발급 및 확인 요청값을 검증한다. */
public class EmailVerificationRequest {

    @NotBlank
    @Email
    @Size(max = 255)
    private String email;

    @Pattern(regexp = "\\d{6}")
    private String code;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email == null ? null : email.trim(); }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}
