package com.okerry.platform.portal;

import java.util.List;

public record PortalServiceListResult(
        String displayName,
        List<PortalServiceCard> availableServices,
        boolean lookupFailed
) {

    public static PortalServiceListResult empty(String displayName) {
        return new PortalServiceListResult(displayName, List.of(), false);
    }

    public static PortalServiceListResult failed(String displayName) {
        return new PortalServiceListResult(displayName, List.of(), true);
    }
}
