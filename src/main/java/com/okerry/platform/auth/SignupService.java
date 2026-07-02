package com.okerry.platform.auth;

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
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 회원가입 사용자, 비밀번호 인증 정보, 기본 권한 연결을 하나의 트랜잭션으로 저장한다.
 */
@Service
@Profile("db")
public class SignupService {

    static final String DEFAULT_ROLE_CODE = "AUTH_USER";

    private final AuthUserRepository userRepository;
    private final AuthUserCredentialRepository credentialRepository;
    private final AuthRoleRepository roleRepository;
    private final AuthUserRoleRepository userRoleRepository;
    private final PasswordHashService passwordHashService;

    public SignupService(
            AuthUserRepository userRepository,
            AuthUserCredentialRepository credentialRepository,
            AuthRoleRepository roleRepository,
            AuthUserRoleRepository userRoleRepository,
            PasswordHashService passwordHashService) {
        this.userRepository = userRepository;
        this.credentialRepository = credentialRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordHashService = passwordHashService;
    }

    @Transactional
    public void signup(SignupRequest request) {
        String email = normalizeEmail(request.getEmail());
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException();
        }
        if (request.getPassword().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new InvalidPasswordException();
        }

        AuthRole defaultRole = roleRepository.findByCode(DEFAULT_ROLE_CODE)
                .orElseThrow(() -> new RequiredRoleNotFoundException(DEFAULT_ROLE_CODE));
        AuthUser user = new AuthUser(email, request.getName().trim(), UserStatus.ACTIVE);

        try {
            userRepository.saveAndFlush(user);
            credentialRepository.save(new AuthUserCredential(
                    user,
                    passwordHashService.hash(request.getPassword()),
                    PasswordHashService.ALGORITHM));
            userRoleRepository.save(new AuthUserRole(user, defaultRole));
            userRoleRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new SignupPersistenceException(exception);
        }
    }

    static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
