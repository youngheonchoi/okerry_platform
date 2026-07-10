package com.okerry.platform.serviceaccess;

import jakarta.servlet.http.HttpSession;
import java.security.Principal;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Profile("neon")
@RequestMapping("/service-access")
public class ServiceAccessRequestController {

    private final ServiceAccessRequestMailService mailService;

    public ServiceAccessRequestController(ServiceAccessRequestMailService mailService) {
        this.mailService = mailService;
    }

    @GetMapping("/request")
    public String request(
            @RequestParam(name = "request_id", required = false) String requestId,
            HttpSession session,
            Model model,
            Principal principal) {
        PendingServiceAccessRequest pendingRequest = findPendingRequest(session, requestId, principal);
        if (pendingRequest == null) {
            return "redirect:/portal/services";
        }
        model.addAttribute("accessRequest", pendingRequest);
        model.addAttribute("mailSent", false);
        model.addAttribute("mailFailed", false);
        return "service-access/request";
    }

    @PostMapping("/request")
    public String submit(
            @RequestParam(name = "request_id", required = false) String requestId,
            HttpSession session,
            Model model,
            Principal principal) {
        PendingServiceAccessRequest pendingRequest = findPendingRequest(session, requestId, principal);
        if (pendingRequest == null) {
            return "redirect:/portal/services";
        }

        model.addAttribute("accessRequest", pendingRequest);
        try {
            mailService.sendRequest(pendingRequest);
            session.removeAttribute(pendingRequest.sessionAttributeName());
            model.addAttribute("mailSent", true);
            model.addAttribute("mailFailed", false);
        } catch (RuntimeException exception) {
            model.addAttribute("mailSent", false);
            model.addAttribute("mailFailed", true);
        }
        return "service-access/request";
    }

    @GetMapping("/unavailable")
    public String unavailable() {
        return "service-access/unavailable";
    }

    private PendingServiceAccessRequest findPendingRequest(
            HttpSession session,
            String requestId,
            Principal principal) {
        if (requestId == null || requestId.isBlank() || principal == null) {
            return null;
        }
        Object value = session.getAttribute(PendingServiceAccessRequest.SESSION_ATTRIBUTE_PREFIX + requestId);
        if (!(value instanceof PendingServiceAccessRequest pendingRequest)) {
            return null;
        }
        if (!pendingRequest.userEmail().equalsIgnoreCase(principal.getName())) {
            return null;
        }
        return pendingRequest;
    }
}
