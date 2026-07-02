package com.okerry.platform.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.autoconfigure.exclude=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class AuthPersistenceTest {

    @Autowired
    private AuthUserRepository userRepository;

    @Autowired
    private AuthUserCredentialRepository credentialRepository;

    @Autowired
    private AuthRoleRepository roleRepository;

    @Autowired
    private AuthUserRoleRepository userRoleRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void storesAndFindsUserCredentialAndRole() {
        AuthUser user = userRepository.save(new AuthUser(
                "user@example.com", "테스트 사용자", UserStatus.ACTIVE));
        AuthRole role = roleRepository.save(new AuthRole(
                "AUTH_USER", "일반 사용자", "SSO 일반 인증 사용자"));
        AuthUserCredential credential = credentialRepository.save(
                new AuthUserCredential(user, "$2a$12$hashed-password", "BCRYPT"));
        userRoleRepository.save(new AuthUserRole(user, role));

        entityManager.flush();
        entityManager.clear();

        assertThat(userRepository.findByEmail("user@example.com"))
                .get().extracting(AuthUser::getPublicId).isEqualTo(user.getPublicId());
        assertThat(credentialRepository.findByUserId(user.getId()))
                .get().extracting(AuthUserCredential::getPasswordHash)
                .isEqualTo(credential.getPasswordHash());
        List<AuthUserRole> roles = userRoleRepository.findAllByUserId(user.getId());
        assertThat(roles).hasSize(1);
        assertThat(roles.getFirst().getRole().getCode()).isEqualTo("AUTH_USER");
    }

    @Test
    void rejectsDuplicateEmail() {
        userRepository.saveAndFlush(new AuthUser(
                "duplicate@example.com", "첫 사용자", UserStatus.ACTIVE));

        assertThatThrownBy(() -> userRepository.saveAndFlush(new AuthUser(
                "duplicate@example.com", "두 번째 사용자", UserStatus.ACTIVE)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejectsMoreThanOneCredentialForUser() {
        AuthUser user = userRepository.save(new AuthUser(
                "credential@example.com", "인증 사용자", UserStatus.ACTIVE));
        credentialRepository.saveAndFlush(new AuthUserCredential(
                user, "$2a$12$first-hash", "BCRYPT"));

        assertThatThrownBy(() -> credentialRepository.saveAndFlush(new AuthUserCredential(
                user, "$2a$12$second-hash", "BCRYPT")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
