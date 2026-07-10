package com.okerry.platform.portal;

import com.okerry.platform.identity.UserStatus;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ServiceUserRepository extends JpaRepository<ServiceUser, Long> {

    @Query("""
            select new com.okerry.platform.portal.PortalServiceCard(
                service.name,
                service.description,
                service.logoUrl,
                service.loginStartUrl,
                serviceUser.status
            )
            from ServiceUser serviceUser
                join serviceUser.user user
                join serviceUser.service service
            where user.id = :userId
                and user.status = :userStatus
                and serviceUser.deletedAt is null
                and serviceUser.status in :serviceUserStatuses
                and service.enabled = true
            order by service.displayOrder asc, service.name asc
            """)
    List<PortalServiceCard> findAvailableServices(
            @Param("userId") Long userId,
            @Param("userStatus") UserStatus userStatus,
            @Param("serviceUserStatuses") Collection<ServiceUserStatus> serviceUserStatuses);

    @Query("""
            select count(serviceUser) > 0
            from ServiceUser serviceUser
            where serviceUser.user.id = :userId
                and serviceUser.service.id = :serviceId
                and serviceUser.deletedAt is null
                and serviceUser.status in :serviceUserStatuses
                and serviceUser.service.enabled = true
            """)
    boolean existsAccessibleServiceUser(
            @Param("userId") Long userId,
            @Param("serviceId") Long serviceId,
            @Param("serviceUserStatuses") Collection<ServiceUserStatus> serviceUserStatuses);
}
