package com.okerry.platform.identity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PasswordHashServiceTest {

    private final PasswordHashService passwordHashService = new PasswordHashService();

    @Test
    void hashesPasswordWithoutStoringPlainText() {
        String rawPassword = "plain-password";

        String passwordHash = passwordHashService.hash(rawPassword);

        assertThat(passwordHash).isNotEqualTo(rawPassword);
        assertThat(passwordHash).startsWith("$2");
        assertThat(passwordHashService.matches(rawPassword, passwordHash)).isTrue();
        assertThat(passwordHashService.matches("wrong-password", passwordHash)).isFalse();
    }
}
