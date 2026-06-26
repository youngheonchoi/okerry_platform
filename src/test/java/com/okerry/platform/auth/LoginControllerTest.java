package com.okerry.platform.auth;

import static org.hamcrest.Matchers.containsString;
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
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void loginPageRenders() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("okerry platform")))
                .andExpect(content().string(containsString("/css/platform_style.css")))
                .andExpect(content().string(containsString("name=\"username\"")))
                .andExpect(content().string(containsString("name=\"password\"")));
    }

    @Test
    void unauthenticatedRequestRedirectsToLoginPage() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void invalidLoginRedirectsToLoginPageWithError() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "user@example.com")
                        .param("password", "wrong-password")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
    }
}
