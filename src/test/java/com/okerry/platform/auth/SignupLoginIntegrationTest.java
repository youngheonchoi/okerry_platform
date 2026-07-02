package com.okerry.platform.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.okerry.platform.identity.AuthRole;
import com.okerry.platform.identity.AuthRoleRepository;
import com.okerry.platform.identity.AuthUser;
import com.okerry.platform.identity.AuthUserCredential;
import com.okerry.platform.identity.AuthUserCredentialRepository;
import com.okerry.platform.identity.AuthUserRepository;
import com.okerry.platform.identity.AuthUserRole;
import com.okerry.platform.identity.AuthUserRoleRepository;
import com.okerry.platform.identity.PasswordHashService;
import com.okerry.platform.identity.UserStatus;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("neon")
@TestPropertySource(properties = {
        "spring.autoconfigure.exclude=",
        "spring.datasource.url=jdbc:h2:mem:signup-login;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class SignupLoginIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthUserRepository userRepository;

    @Autowired
    private AuthUserCredentialRepository credentialRepository;

    @Autowired
    private AuthRoleRepository roleRepository;

    @Autowired
    private AuthUserRoleRepository userRoleRepository;

    @Autowired
    private PasswordHashService passwordHashService;

    @BeforeEach
    void setUp() {
        userRoleRepository.deleteAll();
        credentialRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
        roleRepository.save(new AuthRole("AUTH_USER", "일반 사용자", "SSO 일반 인증 사용자"));
    }

    @Test
    void signupStoresHashedPasswordAndUserCanLogin() throws Exception {
        mockMvc.perform(post("/signup")
                        .with(csrf())
                        .sessionAttr(EmailVerificationController.VERIFIED_EMAIL_SESSION_KEY, "user@example.com")
                        .param("name", " 오케리 ")
                        .param("email", " User@Example.COM ")
                        .param("password", "safe-password-123")
                        .param("passwordConfirmation", "safe-password-123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));

        AuthUser user = userRepository.findByEmail("user@example.com").orElseThrow();
        AuthUserCredential credential = credentialRepository.findByUserId(user.getId()).orElseThrow();
        assertThat(user.getName()).isEqualTo("오케리");
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.isEmailVerified()).isTrue();
        assertThat(credential.getPasswordHash()).isNotEqualTo("safe-password-123");
        assertThat(passwordHashService.matches("safe-password-123", credential.getPasswordHash())).isTrue();
        assertThat(userRoleRepository.findRoleCodesByUserId(user.getId())).containsExactly("AUTH_USER");

        HttpSession session = mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "USER@example.com")
                        .param("password", "safe-password-123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/portal/services"))
                .andReturn()
                .getRequest()
                .getSession(false);

        assertThat(session).isNotNull();
        assertThat(userRepository.findById(user.getId()).orElseThrow().getLastLoginAt()).isNotNull();

        mockMvc.perform(post("/logout").session((org.springframework.mock.web.MockHttpSession) session).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void duplicateEmailAndInvalidInputAreRejected() throws Exception {
        performSignup("user@example.com", "safe-password-123");

        mockMvc.perform(post("/signup")
                        .with(csrf())
                        .sessionAttr(EmailVerificationController.VERIFIED_EMAIL_SESSION_KEY, "user@example.com")
                        .param("name", "다른 사용자")
                        .param("email", "USER@example.com")
                        .param("password", "another-password-123")
                        .param("passwordConfirmation", "another-password-123"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("이미 가입된 이메일입니다.")));

        mockMvc.perform(post("/signup")
                        .with(csrf())
                        .sessionAttr(EmailVerificationController.VERIFIED_EMAIL_SESSION_KEY, "user@example.com")
                        .param("name", "사용자")
                        .param("email", "invalid-email")
                        .param("password", "safe-password-123")
                        .param("passwordConfirmation", "different-password"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("올바른 이메일 형식")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("일치하지 않습니다")));

        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    void wrongPasswordAndDisabledAccountCannotLogin() throws Exception {
        performSignup("active@example.com", "safe-password-123");
        createUser("disabled@example.com", UserStatus.DISABLED, "safe-password-123");
        AuthUser lockedUser = createUser("locked@example.com", UserStatus.ACTIVE, "safe-password-123");
        AuthUserCredential lockedCredential = credentialRepository.findByUserId(lockedUser.getId()).orElseThrow();
        lockedCredential.lockUntil(LocalDateTime.now().plusMinutes(10));
        credentialRepository.save(lockedCredential);

        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "active@example.com")
                        .param("password", "wrong-password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));

        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "disabled@example.com")
                        .param("password", "safe-password-123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));

        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "locked@example.com")
                        .param("password", "safe-password-123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void signupRollsBackWhenDefaultRoleIsMissing() throws Exception {
        roleRepository.deleteAll();

        mockMvc.perform(post("/signup")
                        .with(csrf())
                        .sessionAttr(EmailVerificationController.VERIFIED_EMAIL_SESSION_KEY,
                                "user@example.com")
                        .param("name", "사용자")
                        .param("email", "user@example.com")
                        .param("password", "safe-password-123")
                        .param("passwordConfirmation", "safe-password-123"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("현재 회원가입을 처리할 수 없습니다.")));

        assertThat(userRepository.count()).isZero();
        assertThat(credentialRepository.count()).isZero();
        assertThat(userRoleRepository.count()).isZero();
    }

    @Test
    void signupRequiresVerifiedEmailInSameSession() throws Exception {
        mockMvc.perform(post("/signup")
                        .with(csrf())
                        .param("name", "사용자")
                        .param("email", "user@example.com")
                        .param("password", "safe-password-123")
                        .param("passwordConfirmation", "safe-password-123"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("이메일 인증을 완료해주세요.")));

        assertThat(userRepository.count()).isZero();
    }

    private void performSignup(String email, String password) throws Exception {
        mockMvc.perform(post("/signup")
                        .with(csrf())
                        .sessionAttr(EmailVerificationController.VERIFIED_EMAIL_SESSION_KEY,
                                SignupService.normalizeEmail(email))
                        .param("name", "사용자")
                        .param("email", email)
                        .param("password", password)
                        .param("passwordConfirmation", password))
                .andExpect(status().is3xxRedirection());
    }

    private AuthUser createUser(String email, UserStatus status, String password) {
        AuthUser user = userRepository.save(new AuthUser(email, "사용자", status));
        credentialRepository.save(new AuthUserCredential(
                user, passwordHashService.hash(password), PasswordHashService.ALGORITHM));
        AuthRole role = roleRepository.findByCode("AUTH_USER").orElseThrow();
        userRoleRepository.save(new AuthUserRole(user, role));
        return user;
    }
}
