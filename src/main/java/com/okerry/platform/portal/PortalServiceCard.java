package com.okerry.platform.portal;

public record PortalServiceCard(
        String name,
        String description,
        String logoUrl,
        String entryPoint,
        ServiceUserStatus status
) {

    public boolean pendingProfile() {
        return status == ServiceUserStatus.PENDING_PROFILE;
    }
}
