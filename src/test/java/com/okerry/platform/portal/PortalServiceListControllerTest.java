package com.okerry.platform.portal;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.okerry.platform.identity.AuthUser;
import com.okerry.platform.identity.AuthUserRepository;
import com.okerry.platform.identity.UserStatus;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("neon")
@TestPropertySource(properties = {
        "spring.autoconfigure.exclude=",
        "spring.datasource.url=jdbc:h2:mem:portal-services;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class PortalServiceListControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthUserRepository userRepository;

    @Autowired
    private RegisteredServiceRepository registeredServiceRepository;

    @Autowired
    private ServiceUserRepository serviceUserRepository;

    @BeforeEach
    void setUp() {
        serviceUserRepository.deleteAll();
        registeredServiceRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void unauthenticatedPortalRequestRedirectsToLoginPage() throws Exception {
        mockMvc.perform(get("/portal/services"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void rootRequestRedirectsToPortalServices() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/portal/services"));
    }

    @Test
    @WithMockUser(username = "USER@example.com")
    void serviceListPageRendersOnlyServicesAvailableToAuthenticatedUser() throws Exception {
        AuthUser user = userRepository.save(new AuthUser("user@example.com", "오케리", UserStatus.ACTIVE));
        AuthUser otherUser = userRepository.save(
                new AuthUser("other@example.com", "다른 사용자", UserStatus.ACTIVE));

        RegisteredService alpha = saveService("alpha", "Alpha Service", 2, true);
        RegisteredService beta = saveService("beta", "Beta Service", 1, true);
        RegisteredService gamma = saveService("gamma", "Gamma Service", 1, true);
        RegisteredService disabled = saveService("disabled", "Disabled Service", 0, false);
        RegisteredService otherOnly = saveService("other-only", "Other User Service", 0, true);
        RegisteredService deleted = saveService("deleted", "Deleted Link Service", 0, true);
        RegisteredService suspended = saveService("suspended", "Suspended Service", 0, true);
        RegisteredService withdrawn = saveService("withdrawn", "Withdrawn Service", 0, true);

        serviceUserRepository.save(new ServiceUser(user, alpha, ServiceUserStatus.ACTIVE));
        serviceUserRepository.save(new ServiceUser(user, beta, ServiceUserStatus.PENDING_PROFILE));
        serviceUserRepository.save(new ServiceUser(user, gamma, ServiceUserStatus.ACTIVE));
        serviceUserRepository.save(new ServiceUser(user, disabled, ServiceUserStatus.ACTIVE));
        serviceUserRepository.save(new ServiceUser(otherUser, otherOnly, ServiceUserStatus.ACTIVE));
        ServiceUser deletedLink = serviceUserRepository.save(
                new ServiceUser(user, deleted, ServiceUserStatus.ACTIVE));
        deletedLink.delete(LocalDateTime.now());
        serviceUserRepository.save(deletedLink);
        serviceUserRepository.save(new ServiceUser(user, suspended, ServiceUserStatus.SUSPENDED));
        serviceUserRepository.save(new ServiceUser(user, withdrawn, ServiceUserStatus.WITHDRAWN));

        mockMvc.perform(get("/portal/services"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("오케리님 환영합니다.")))
                .andExpect(content().string(containsString("로그아웃")))
                .andExpect(content().string(containsString("action=\"/logout\"")))
                .andExpect(content().string(containsString("name=\"_csrf\"")))
                .andExpect(content().string(containsString("접근 가능 서비스")))
                .andExpect(content().string(containsString("Alpha Service")))
                .andExpect(content().string(containsString("Beta Service")))
                .andExpect(content().string(containsString("Gamma Service")))
                .andExpect(content().string(containsString("프로필 필요")))
                .andExpect(content().string(containsString("href=\"https://beta.example.test/login\"")))
                .andExpect(content().string(stringContainsInOrder(
                        "Beta Service",
                        "Gamma Service",
                        "Alpha Service"
                )))
                .andExpect(content().string(containsString("서비스 이동")))
                .andExpect(content().string(not(containsString("Disabled Service"))))
                .andExpect(content().string(not(containsString("Other User Service"))))
                .andExpect(content().string(not(containsString("Deleted Link Service"))))
                .andExpect(content().string(not(containsString("Suspended Service"))))
                .andExpect(content().string(not(containsString("Withdrawn Service"))));
    }

    @Test
    @WithMockUser(username = "empty@example.com")
    void serviceListPageRendersEmptyStateWhenUserHasNoAvailableServices() throws Exception {
        userRepository.save(new AuthUser("empty@example.com", "빈 사용자", UserStatus.ACTIVE));

        mockMvc.perform(get("/portal/services"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("빈 사용자님 환영합니다.")))
                .andExpect(content().string(containsString("현재 접근 가능한 서비스가 없습니다.")))
                .andExpect(content().string(not(containsString("서비스 이동"))));
    }

    @Test
    @WithMockUser(username = "missing@example.com")
    void serviceListPageDoesNotExposeDetailsWhenPrincipalDoesNotMatchUser() throws Exception {
        mockMvc.perform(get("/portal/services"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("현재 접근 가능한 서비스가 없습니다.")))
                .andExpect(content().string(not(containsString("missing@example.com님 환영합니다."))));
    }

    @Test
    @WithMockUser(username = "gjswns21@gmail.com")
    void serviceListPageUsesMemberNameInsteadOfEmail() throws Exception {
        userRepository.save(new AuthUser("gjswns21@gmail.com", "회원이름", UserStatus.ACTIVE));

        mockMvc.perform(get("/portal/services"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("회원이름님 환영합니다.")))
                .andExpect(content().string(not(containsString("gjswns21@gmail.com님 환영합니다."))));
    }

    @Test
    @WithMockUser(username = "tester")
    void logoutFromPortalRedirectsToLoginPage() throws Exception {
        mockMvc.perform(post("/logout").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void loginPageDoesNotExposePortalScreenToAnonymousUser() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("접근 가능 서비스"))));
    }

    private RegisteredService saveService(String key, String name, int displayOrder, boolean enabled) {
        return registeredServiceRepository.save(new RegisteredService(
                key,
                name,
                name + " 설명",
                "https://" + key + ".example.test",
                "https://" + key + ".example.test/login",
                "https://" + key + ".example.test/logo.png",
                key + "-client",
                displayOrder,
                enabled));
    }
}
