package com.okerry.platform.portal;

import com.okerry.platform.identity.AuthUser;
import com.okerry.platform.identity.AuthUserRepository;
import com.okerry.platform.identity.UserStatus;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile("neon")
public class PortalServiceListQuery {

    private static final Logger log = LoggerFactory.getLogger(PortalServiceListQuery.class);
    private static final List<ServiceUserStatus> ACCESSIBLE_STATUSES =
            List.of(ServiceUserStatus.ACTIVE, ServiceUserStatus.PENDING_PROFILE);

    private final AuthUserRepository userRepository;
    private final ServiceUserRepository serviceUserRepository;

    public PortalServiceListQuery(
            AuthUserRepository userRepository,
            ServiceUserRepository serviceUserRepository) {
        this.userRepository = userRepository;
        this.serviceUserRepository = serviceUserRepository;
    }

    @Transactional(readOnly = true)
    public PortalServiceListResult findAvailableServices(String principalName) {
        String email = normalizeEmail(principalName);
        try {
            return userRepository.findByEmail(email)
                    .filter(user -> user.getStatus() == UserStatus.ACTIVE)
                    .map(this::findForActiveUser)
                    .orElseGet(() -> PortalServiceListResult.empty("사용자"));
        } catch (DataAccessException exception) {
            log.warn("Failed to load portal service list for authenticated user.");
            return PortalServiceListResult.failed("사용자");
        }
    }

    private PortalServiceListResult findForActiveUser(AuthUser user) {
        List<PortalServiceCard> services = serviceUserRepository.findAvailableServices(
                user.getId(), UserStatus.ACTIVE, ACCESSIBLE_STATUSES);
        return new PortalServiceListResult(user.getName(), services, false);
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return "";
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
