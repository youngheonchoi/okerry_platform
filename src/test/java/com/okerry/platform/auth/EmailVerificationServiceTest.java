package com.okerry.platform.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.okerry.platform.identity.AuthUserRepository;
import com.okerry.platform.identity.PasswordHashService;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    @Mock
    private EmailVerificationRepository verificationRepository;
    @Mock
    private AuthUserRepository userRepository;
    @Mock
    private PasswordHashService passwordHashService;
    @Mock
    private MailSender mailSender;

    private EmailVerificationService service;

    @BeforeEach
    void setUp() {
        service = new EmailVerificationService(
                verificationRepository, userRepository, passwordHashService, mailSender);
    }

    @Test
    void sendsSixDigitCodeAndStoresOnlyHash() {
        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(verificationRepository.findTopByEmailOrderByCreatedAtDesc("user@example.com"))
                .thenReturn(Optional.empty());
        when(passwordHashService.hash(anyString())).thenReturn("hashed-code");

        service.sendCode("USER@example.com");

        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
        verify(passwordHashService).hash(codeCaptor.capture());
        assertThat(codeCaptor.getValue()).matches("\\d{6}");

        ArgumentCaptor<EmailVerification> verificationCaptor =
                ArgumentCaptor.forClass(EmailVerification.class);
        verify(verificationRepository).save(verificationCaptor.capture());
        assertThat(verificationCaptor.getValue().getCodeHash()).isEqualTo("hashed-code");

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        assertThat(messageCaptor.getValue().getTo()).containsExactly("user@example.com");
    }

    @Test
    void verifiesValidCodeAndLimitsResend() {
        LocalDateTime now = LocalDateTime.now();
        EmailVerification verification = new EmailVerification("user@example.com", "hash", now);
        when(verificationRepository.findTopByEmailOrderByCreatedAtDesc("user@example.com"))
                .thenReturn(Optional.of(verification));
        when(passwordHashService.matches("123456", "hash")).thenReturn(true);

        assertThat(service.verifyCode("user@example.com", "123456")).isTrue();
        assertThat(verification.getVerifiedAt()).isNotNull();

        assertThatThrownBy(() -> service.sendCode("user@example.com"))
                .isInstanceOf(EmailVerificationRateLimitException.class);
    }
}
