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

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class PortalServiceListControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void unauthenticatedPortalRequestRedirectsToLoginPage() throws Exception {
        mockMvc.perform(get("/portal/services"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser(username = "오케리")
    void serviceListPageRendersForAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/portal/services"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("오케리님 환영합니다.")))
                .andExpect(content().string(containsString("로그아웃")))
                .andExpect(content().string(containsString("action=\"/logout\"")))
                .andExpect(content().string(containsString("name=\"_csrf\"")))
                .andExpect(content().string(containsString("접근 가능 서비스")))
                .andExpect(content().string(containsString("Okerry Notes")))
                .andExpect(content().string(containsString("Okerry Reports")))
                .andExpect(content().string(stringContainsInOrder(
                        "Okerry Notes",
                        "Okerry Tasks",
                        "Okerry Admin",
                        "Okerry Docs",
                        "Okerry Reports",
                        "Okerry Community",
                        "Okerry Assets"
                )))
                .andExpect(content().string(containsString("서비스 이동")));
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
}
