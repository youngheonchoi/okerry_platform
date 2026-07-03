package com.okerry.platform.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
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
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.util.UriComponentsBuilder;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("neon")
@TestPropertySource(properties = {
        "spring.autoconfigure.exclude=",
        "spring.datasource.url=jdbc:h2:mem:authorization-server;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "platform.oauth2.issuer=http://localhost"
})
@Sql(scripts = "/oauth2-test-schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class AuthorizationServerIntegrationTest {

    private static final String CLIENT_ID = "integration-client";
    private static final String CLIENT_SECRET = "integration-secret";
    private static final String REDIRECT_URI = "https://client.example.test/login/oauth2/code/okerry";
    private static final String VERIFIER = "0123456789012345678901234567890123456789012345678901234567890123";

    @Autowired MockMvc mockMvc;
    @Autowired RegisteredClientRepository registeredClientRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired PasswordHashService passwordHashService;
    @Autowired AuthUserRepository userRepository;
    @Autowired AuthUserCredentialRepository credentialRepository;
    @Autowired AuthRoleRepository roleRepository;
    @Autowired AuthUserRoleRepository userRoleRepository;
    @Autowired JwtDecoder jwtDecoder;
    @Autowired JdbcTemplate jdbcTemplate;

    private AuthUser user;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM oauth2_authorization_consent");
        jdbcTemplate.update("DELETE FROM oauth2_authorization");
        jdbcTemplate.update("DELETE FROM oauth2_registered_client");
        userRoleRepository.deleteAll();
        credentialRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        AuthRole role = roleRepository.save(
                new AuthRole("AUTH_USER", "일반 사용자", "SSO 일반 인증 사용자"));
        user = new AuthUser("oidc-user@example.com", "OIDC 사용자", UserStatus.ACTIVE);
        user.verifyEmail();
        userRepository.save(user);
        credentialRepository.save(new AuthUserCredential(
                user, passwordHashService.hash("safe-password-123"), PasswordHashService.ALGORITHM));
        userRoleRepository.save(new AuthUserRole(user, role));

        RegisteredClient client = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId(CLIENT_ID)
                .clientSecret(passwordEncoder.encode(CLIENT_SECRET))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri(REDIRECT_URI)
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)
                .scope(OidcScopes.EMAIL)
                .clientSettings(ClientSettings.builder()
                        .requireProofKey(true)
                        .requireAuthorizationConsent(false)
                        .build())
                .tokenSettings(TokenSettings.builder()
                        .authorizationCodeTimeToLive(Duration.ofMinutes(2))
                        .accessTokenTimeToLive(Duration.ofMinutes(5))
                        .refreshTokenTimeToLive(Duration.ofDays(30))
                        .reuseRefreshTokens(false)
                        .build())
                .build();
        registeredClientRepository.save(client);
    }

    @Test
    void discoveryAndJwkSetArePublic() throws Exception {
        mockMvc.perform(get("/.well-known/openid-configuration"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/oauth2/jwks"))
                .andExpect(status().isOk());
    }

    @Test
    void authorizationCodeWithPkceIssuesOidcTokensAndRejectsCodeReuse() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(get("/oauth2/authorize")
                        .session(session)
                        .accept(MediaType.TEXT_HTML)
                        .queryParam("response_type", "code")
                        .queryParam("client_id", CLIENT_ID)
                        .queryParam("redirect_uri", REDIRECT_URI)
                        .queryParam("scope", "openid profile email")
                        .queryParam("state", "expected-state")
                        .queryParam("code_challenge", challenge(VERIFIER))
                        .queryParam("code_challenge_method", "S256"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/login"));

        String savedAuthorizationUri = mockMvc.perform(post("/login")
                        .session(session)
                        .with(csrf())
                        .param("username", user.getEmail())
                        .param("password", "safe-password-123"))
                .andExpect(status().is3xxRedirection())
                .andReturn().getResponse().getRedirectedUrl();

        String clientRedirect = mockMvc.perform(get(URI.create(savedAuthorizationUri)).session(session))
                .andExpect(status().is3xxRedirection())
                .andReturn().getResponse().getRedirectedUrl();
        URI redirect = URI.create(clientRedirect);
        String code = UriComponentsBuilder.fromUri(redirect).build().getQueryParams().getFirst("code");
        assertThat(UriComponentsBuilder.fromUri(redirect).build().getQueryParams().getFirst("state"))
                .isEqualTo("expected-state");

        MvcResult tokenResult = mockMvc.perform(post("/oauth2/token")
                        .with(httpBasic(CLIENT_ID, CLIENT_SECRET))
                        .param("grant_type", "authorization_code")
                        .param("code", code)
                        .param("redirect_uri", REDIRECT_URI)
                        .param("code_verifier", VERIFIER))
                .andExpect(status().isOk())
                .andReturn();
        String tokenResponse = tokenResult.getResponse().getContentAsString();
        String accessToken = JsonPath.read(tokenResponse, "$.access_token");
        String idToken = JsonPath.read(tokenResponse, "$.id_token");
        String refreshToken = JsonPath.read(tokenResponse, "$.refresh_token");
        assertThat(refreshToken).isNotBlank();

        Jwt jwt = jwtDecoder.decode(idToken);
        assertThat(jwt.getSubject()).isEqualTo(user.getPublicId());
        assertThat(jwt.getClaimAsString("email")).isEqualTo(user.getEmail());
        assertThat(jwt.getClaimAsString("name")).isEqualTo(user.getName());

        mockMvc.perform(get("/userinfo").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sub").value(user.getPublicId()))
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.name").value(user.getName()));

        String refreshedResponse = mockMvc.perform(post("/oauth2/token")
                        .with(httpBasic(CLIENT_ID, CLIENT_SECRET))
                        .param("grant_type", "refresh_token")
                        .param("refresh_token", refreshToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String rotatedRefreshToken = JsonPath.read(refreshedResponse, "$.refresh_token");
        assertThat(rotatedRefreshToken).isNotEqualTo(refreshToken);

        mockMvc.perform(post("/oauth2/token")
                        .with(httpBasic(CLIENT_ID, CLIENT_SECRET))
                        .param("grant_type", "refresh_token")
                        .param("refresh_token", refreshToken))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/oauth2/token")
                        .with(httpBasic(CLIENT_ID, CLIENT_SECRET))
                        .param("grant_type", "authorization_code")
                        .param("code", code)
                        .param("redirect_uri", REDIRECT_URI)
                        .param("code_verifier", VERIFIER))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsInvalidRedirectUriAndMissingPkce() throws Exception {
        mockMvc.perform(get("/oauth2/authorize")
                        .queryParam("response_type", "code")
                        .queryParam("client_id", CLIENT_ID)
                        .queryParam("redirect_uri", "https://attacker.example.test/callback")
                        .queryParam("scope", "openid"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/oauth2/authorize")
                        .queryParam("response_type", "code")
                        .queryParam("client_id", CLIENT_ID)
                        .queryParam("redirect_uri", REDIRECT_URI)
                        .queryParam("scope", "openid"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("error=invalid_request")))
                .andExpect(header().string("Location", containsString("code_challenge")));
    }

    private static String challenge(String verifier) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(verifier.getBytes(StandardCharsets.US_ASCII));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
    }
}
