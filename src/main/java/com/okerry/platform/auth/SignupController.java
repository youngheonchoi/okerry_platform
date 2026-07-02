package com.okerry.platform.auth;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class SignupController {

    private final ObjectProvider<SignupService> signupServiceProvider;

    public SignupController(ObjectProvider<SignupService> signupServiceProvider) {
        this.signupServiceProvider = signupServiceProvider;
    }

    @GetMapping("/signup")
    public String signup(Model model) {
        if (!model.containsAttribute("signupRequest")) {
            model.addAttribute("signupRequest", new SignupRequest());
        }
        return "auth/signup";
    }

    @PostMapping("/signup")
    public String signup(
            @Valid @ModelAttribute("signupRequest") SignupRequest request,
            BindingResult bindingResult,
            HttpSession session) {
        if (bindingResult.hasErrors()) {
            return "auth/signup";
        }

        SignupService signupService = signupServiceProvider.getIfAvailable();
        if (signupService == null) {
            bindingResult.reject("signup.unavailable", "현재 회원가입을 처리할 수 없습니다.");
            return "auth/signup";
        }
        Object verifiedEmail = session.getAttribute(EmailVerificationController.VERIFIED_EMAIL_SESSION_KEY);
        if (!SignupService.normalizeEmail(request.getEmail()).equals(verifiedEmail)) {
            bindingResult.rejectValue("email", "signup.email.unverified", "이메일 인증을 완료해주세요.");
            return "auth/signup";
        }

        try {
            signupService.signup(request);
        } catch (DuplicateEmailException exception) {
            bindingResult.rejectValue("email", "signup.email.duplicate", "이미 가입된 이메일입니다.");
            return "auth/signup";
        } catch (InvalidPasswordException exception) {
            bindingResult.rejectValue("password", "signup.password.invalid", "사용할 수 없는 비밀번호입니다.");
            return "auth/signup";
        } catch (RequiredRoleNotFoundException exception) {
            bindingResult.reject("signup.configuration", "현재 회원가입을 처리할 수 없습니다.");
            return "auth/signup";
        } catch (SignupPersistenceException exception) {
            bindingResult.reject("signup.persistence", "회원가입을 처리하지 못했습니다. 다시 시도해주세요.");
            return "auth/signup";
        }

        session.removeAttribute(EmailVerificationController.VERIFIED_EMAIL_SESSION_KEY);
        return "redirect:/login?registered";
    }
}
