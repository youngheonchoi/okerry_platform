package com.okerry.platform.auth;

import static org.hamcrest.Matchers.containsString;
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
                .andExpect(content().string(stringContainsInOrder(
                        "okerry-ui.css",
                        "/css/platform_style.css")))
                .andExpect(content().string(containsString("data-okerry-theme=\"light\"")))
                .andExpect(content().string(containsString("<body class=\"okerry_ui\" data-okerry-theme=\"light\">")))
                .andExpect(content().string(containsString("id=\"theme-toggle\"")))
                .andExpect(content().string(containsString("src=\"/js/platform_theme.js\"")))
                .andExpect(content().string(containsString("class=\"okerry-input\"")))
                .andExpect(content().string(containsString("name=\"username\"")))
                .andExpect(content().string(containsString("name=\"password\"")))
                .andExpect(content().string(containsString("아이디 비밀번호 찾기는 관리자에게 문의해 주세요.")))
                .andExpect(content().string(containsString("href=\"/signup\"")));
    }

    @Test
    void platformThemeScriptIsServed() throws Exception {
        mockMvc.perform(get("/js/platform_theme.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("DOMContentLoaded")))
                .andExpect(content().string(containsString("applyTheme(\"light\")")))
                .andExpect(content().string(containsString("data-okerry-theme")))
                .andExpect(content().string(containsString("theme-toggle")));
    }

    @Test
    void unauthenticatedRequestRedirectsToLoginPage() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void chromeDevToolsDiscoveryRequestDoesNotTriggerLogin() throws Exception {
        mockMvc.perform(get("/.well-known/appspecific/com.chrome.devtools.json"))
                .andExpect(status().isNotFound());
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
