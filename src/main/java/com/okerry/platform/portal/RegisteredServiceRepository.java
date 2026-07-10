package com.okerry.platform.portal;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegisteredServiceRepository extends JpaRepository<RegisteredService, Long> {

    Optional<RegisteredService> findByOauth2ClientId(String oauth2ClientId);
}
