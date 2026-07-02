package com.okerry.platform.auth;

import com.okerry.platform.identity.AuthUserRepository;
import com.okerry.platform.identity.PasswordHashService;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Gmail SMTP로 인증번호를 전송하고 Neon에 저장된 해시로 인증번호를 검증한다. */
@Service
@Profile("neon")
public class EmailVerificationService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String SENDER = "gjswns21@gmail.com";

    private final EmailVerificationRepository verificationRepository;
    private final AuthUserRepository userRepository;
    private final PasswordHashService passwordHashService;
    private final MailSender mailSender;

    public EmailVerificationService(
            EmailVerificationRepository verificationRepository,
            AuthUserRepository userRepository,
            PasswordHashService passwordHashService,
            MailSender mailSender) {
        this.verificationRepository = verificationRepository;
        this.userRepository = userRepository;
        this.passwordHashService = passwordHashService;
        this.mailSender = mailSender;
    }

    @Transactional
    public void sendCode(String rawEmail) {
        String email = SignupService.normalizeEmail(rawEmail);
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException();
        }
        LocalDateTime now = LocalDateTime.now();
        verificationRepository.findTopByEmailOrderByCreatedAtDesc(email)
                .filter(previous -> previous.getCreatedAt().isAfter(now.minusSeconds(60)))
                .ifPresent(previous -> { throw new EmailVerificationRateLimitException(); });

        String code = String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
        verificationRepository.save(new EmailVerification(email, passwordHashService.hash(code), now));

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(SENDER);
        message.setTo(email);
        message.setSubject("[okerry platform] 이메일 인증번호");
        message.setText("이메일 인증번호는 " + code + " 입니다. 5분 안에 입력해주세요.");
        mailSender.send(message);
    }

    @Transactional
    public boolean verifyCode(String rawEmail, String code) {
        String email = SignupService.normalizeEmail(rawEmail);
        EmailVerification verification = verificationRepository.findTopByEmailOrderByCreatedAtDesc(email)
                .orElse(null);
        LocalDateTime now = LocalDateTime.now();
        if (verification == null || !verification.canAttempt(now)) {
            return false;
        }
        if (!passwordHashService.matches(code, verification.getCodeHash())) {
            verification.recordFailure();
            return false;
        }
        verification.markVerified(now);
        return true;
    }
}
