package com.okerry.platform.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.okerry.platform.identity.AuthUser;
import com.okerry.platform.identity.AuthUserRepository;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.util.StringUtils;

@Configuration
@Profile("neon")
public class AuthorizationServerConfig {

    @Bean
    RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate) {
        return new JdbcRegisteredClientRepository(jdbcTemplate);
    }

    @Bean
    OAuth2AuthorizationService authorizationService(
            JdbcTemplate jdbcTemplate,
            RegisteredClientRepository registeredClientRepository) {
        return new JdbcOAuth2AuthorizationService(jdbcTemplate, registeredClientRepository);
    }

    @Bean
    OAuth2AuthorizationConsentService authorizationConsentService(
            JdbcTemplate jdbcTemplate,
            RegisteredClientRepository registeredClientRepository) {
        return new JdbcOAuth2AuthorizationConsentService(jdbcTemplate, registeredClientRepository);
    }

    @Bean
    AuthorizationServerSettings authorizationServerSettings(
            @Value("${platform.oauth2.issuer:http://localhost:8080}") String issuer) {
        return AuthorizationServerSettings.builder().issuer(issuer).build();
    }

    @Bean
    JWKSource<SecurityContext> jwkSource(
            @Value("${platform.oauth2.keys.public-key:}") String encodedPublicKey,
            @Value("${platform.oauth2.keys.private-key:}") String encodedPrivateKey,
            @Value("${platform.oauth2.keys.key-id:okerry-platform}") String keyId,
            @Value("${platform.oauth2.keys.allow-ephemeral:false}") boolean allowEphemeral) {
        KeyPair keyPair = loadKeyPair(encodedPublicKey, encodedPrivateKey, allowEphemeral);
        RSAKey rsaKey = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                .privateKey((RSAPrivateKey) keyPair.getPrivate())
                .keyID(keyId)
                .build();
        return new ImmutableJWKSet<>(new JWKSet(rsaKey));
    }

    @Bean
    JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    @Bean
    OAuth2TokenCustomizer<JwtEncodingContext> oidcClaimsCustomizer(AuthUserRepository userRepository) {
        return context -> {
            if (!OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())
                    && !"id_token".equals(context.getTokenType().getValue())) {
                return;
            }

            AuthUser user = userRepository.findByEmail(context.getPrincipal().getName()).orElse(null);
            if (user == null) {
                return;
            }

            context.getClaims().subject(user.getPublicId());
            if (context.getAuthorizedScopes().contains(OidcScopes.EMAIL)) {
                context.getClaims().claim("email", user.getEmail());
                context.getClaims().claim("email_verified", user.isEmailVerified());
            }
            if (context.getAuthorizedScopes().contains(OidcScopes.PROFILE)) {
                context.getClaims().claim("name", user.getName());
            }
        };
    }

    private static KeyPair loadKeyPair(
            String encodedPublicKey,
            String encodedPrivateKey,
            boolean allowEphemeral) {
        if (StringUtils.hasText(encodedPublicKey) && StringUtils.hasText(encodedPrivateKey)) {
            try {
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(
                        new X509EncodedKeySpec(decodeKey(encodedPublicKey)));
                RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory.generatePrivate(
                        new PKCS8EncodedKeySpec(decodeKey(encodedPrivateKey)));
                return new KeyPair(publicKey, privateKey);
            } catch (Exception exception) {
                throw new IllegalStateException("OAuth2 RSA signing key is invalid", exception);
            }
        }

        if (!allowEphemeral) {
            throw new IllegalStateException(
                    "OAuth2 RSA signing keys must be supplied through external configuration");
        }

        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (Exception exception) {
            throw new IllegalStateException("Could not generate test OAuth2 RSA signing key", exception);
        }
    }

    private static byte[] decodeKey(String encodedKey) {
        String normalized = encodedKey
                .replaceAll("-----BEGIN [A-Z ]+-----", "")
                .replaceAll("-----END [A-Z ]+-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(normalized);
    }
}
