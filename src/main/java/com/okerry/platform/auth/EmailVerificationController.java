package com.okerry.platform.auth;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailException;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.BindingResult;

/** 회원가입 화면의 인증번호 발급 및 확인 요청을 처리한다. */
@RestController
@RequestMapping("/signup/email-verification")
public class EmailVerificationController {

    public static final String VERIFIED_EMAIL_SESSION_KEY = "signup.verified-email";

    private final ObjectProvider<EmailVerificationService> serviceProvider;

    public EmailVerificationController(ObjectProvider<EmailVerificationService> serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    @PostMapping("/send")
    public EmailVerificationResponse send(
            @Valid @ModelAttribute EmailVerificationRequest request,
            BindingResult bindingResult) {
        if (bindingResult.hasFieldErrors("email")) {
            return new EmailVerificationResponse(false, "올바른 이메일을 입력해주세요.");
        }
        EmailVerificationService service = serviceProvider.getIfAvailable();
        if (service == null) {
            return new EmailVerificationResponse(false, "현재 이메일 인증을 처리할 수 없습니다.");
        }
        try {
            service.sendCode(request.getEmail());
            return new EmailVerificationResponse(true, "인증번호를 전송했습니다.");
        } catch (DuplicateEmailException exception) {
            return new EmailVerificationResponse(false, "이미 가입된 이메일입니다.");
        } catch (EmailVerificationRateLimitException exception) {
            return new EmailVerificationResponse(false, "잠시 후 다시 요청해주세요.");
        } catch (MailException exception) {
            return new EmailVerificationResponse(false, "인증번호를 전송하지 못했습니다.");
        }
    }

    @PostMapping("/confirm")
    public EmailVerificationResponse confirm(
            @Valid @ModelAttribute EmailVerificationRequest request,
            BindingResult bindingResult,
            HttpSession session) {
        if (bindingResult.hasErrors() || request.getCode() == null) {
            return new EmailVerificationResponse(false, "이메일과 6자리 인증번호를 확인해주세요.");
        }
        EmailVerificationService service = serviceProvider.getIfAvailable();
        if (service == null || !service.verifyCode(request.getEmail(), request.getCode())) {
            return new EmailVerificationResponse(false, "인증번호가 올바르지 않거나 만료되었습니다.");
        }
        session.setAttribute(VERIFIED_EMAIL_SESSION_KEY, SignupService.normalizeEmail(request.getEmail()));
        return new EmailVerificationResponse(true, "이메일 인증이 완료되었습니다.");
    }
}
