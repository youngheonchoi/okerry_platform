package com.okerry.platform.portal;

import java.security.Principal;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PortalServiceListController {

    private final ObjectProvider<PortalServiceListQuery> serviceListQuery;

    public PortalServiceListController(ObjectProvider<PortalServiceListQuery> serviceListQuery) {
        this.serviceListQuery = serviceListQuery;
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/portal/services";
    }

    @GetMapping("/portal/services")
    public String services(Model model, Principal principal) {
        String principalName = principal == null ? "" : principal.getName();
        PortalServiceListQuery query = serviceListQuery.getIfAvailable();
        PortalServiceListResult result = query == null
                ? PortalServiceListResult.empty("사용자")
                : query.findAvailableServices(principalName);

        model.addAttribute("displayName", result.displayName());
        model.addAttribute("availableServices", result.availableServices());
        model.addAttribute("serviceLookupFailed", result.lookupFailed());
        return "portal/service-list";
    }
}
