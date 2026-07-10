package com.okerry.platform.serviceaccess;

import com.okerry.platform.identity.AuthUser;
import com.okerry.platform.identity.AuthUserRepository;
import com.okerry.platform.identity.UserStatus;
import com.okerry.platform.portal.RegisteredService;
import com.okerry.platform.portal.RegisteredServiceRepository;
import com.okerry.platform.portal.ServiceUserRepository;
import com.okerry.platform.portal.ServiceUserStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Consumer;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationContext;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("neon")
public class ServiceAccessAuthorizationValidator
        implements Consumer<OAuth2AuthorizationCodeRequestAuthenticationContext> {

    private static final List<ServiceUserStatus> ACCESSIBLE_STATUSES =
            List.of(ServiceUserStatus.ACTIVE, ServiceUserStatus.PENDING_PROFILE);

    private final RegisteredServiceRepository registeredServiceRepository;
    private final AuthUserRepository userRepository;
    private final ServiceUserRepository serviceUserRepository;

    public ServiceAccessAuthorizationValidator(
            RegisteredServiceRepository registeredServiceRepository,
            AuthUserRepository userRepository,
            ServiceUserRepository serviceUserRepository) {
        this.registeredServiceRepository = registeredServiceRepository;
        this.userRepository = userRepository;
        this.serviceUserRepository = serviceUserRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public void accept(OAuth2AuthorizationCodeRequestAuthenticationContext context) {
        OAuth2AuthorizationCodeRequestAuthenticationToken authentication =
                context.getAuthentication();
        RegisteredService service = registeredServiceRepository
                .findByOauth2ClientId(context.getRegisteredClient().getClientId())
                .filter(RegisteredService::isEnabled)
                .orElseThrow(() -> new ServiceAccessUnavailableException(authentication));

        Authentication principal = (Authentication) authentication.getPrincipal();
        if (!isAuthenticatedUser(principal)) {
            return;
        }

        AuthUser user = userRepository.findByEmail(normalizeEmail(principal.getName()))
                .filter(candidate -> candidate.getStatus() == UserStatus.ACTIVE)
                .orElseThrow(() -> new ServiceAccessUnavailableException(authentication));

        boolean accessible = serviceUserRepository.existsAccessibleServiceUser(
                user.getId(), service.getId(), ACCESSIBLE_STATUSES);
        if (!accessible) {
            throw new ServiceAccessDeniedException(createPendingRequest(user, service), authentication);
        }
    }

    private boolean isAuthenticatedUser(Authentication principal) {
        return principal != null
                && principal.isAuthenticated()
                && !(principal instanceof AnonymousAuthenticationToken);
    }

    private PendingServiceAccessRequest createPendingRequest(AuthUser user, RegisteredService service) {
        return new PendingServiceAccessRequest(
                UUID.randomUUID().toString(),
                user.getId(),
                user.getEmail(),
                user.getName(),
                service.getId(),
                service.getName(),
                service.getServiceKey(),
                service.getOauth2ClientId(),
                LocalDateTime.now());
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return "";
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
