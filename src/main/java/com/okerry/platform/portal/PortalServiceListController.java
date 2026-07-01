package com.okerry.platform.portal;

import java.security.Principal;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PortalServiceListController {

    @GetMapping("/portal/services")
    public String services(Model model, Principal principal) {
        String displayName = principal == null ? "사용자" : principal.getName();

        model.addAttribute("displayName", displayName);
        model.addAttribute("availableServices", availableServices());

        return "portal/service-list";
    }

    private List<PortalServiceCard> availableServices() {
        return List.of(
                new PortalServiceCard("Okerry Notes", "개인 메모와 작업 기록을 관리하는 서비스입니다.", "/portal/services"),
                new PortalServiceCard("Okerry Tasks", "할 일과 일정 흐름을 정리하는 작업 관리 서비스입니다.", "/portal/services"),
                new PortalServiceCard("Okerry Admin", "서비스 운영 상태와 설정을 확인하는 관리자 콘솔입니다.", "/portal/services"),
                new PortalServiceCard("Okerry Docs", "프로젝트 문서와 가이드를 열람하는 문서 포털입니다.", "/portal/services"),
                new PortalServiceCard("Okerry Reports", "사용 기록과 주요 지표를 확인하는 리포트 화면입니다.", "/portal/services"),
                new PortalServiceCard("Okerry Community", "서비스 공지와 사용자 소식을 확인하는 커뮤니티 허브입니다.", "/portal/services"),
                new PortalServiceCard("Okerry Community", "서비스 공지와 사용자 소식을 확인하는 커뮤니티 허브입니다.", "/portal/services"),
                new PortalServiceCard("Okerry Community", "서비스 공지와 사용자 소식을 확인하는 커뮤니티 허브입니다.", "/portal/services"),
                new PortalServiceCard("Okerry Community", "서비스 공지와 사용자 소식을 확인하는 커뮤니티 허브입니다.", "/portal/services"),
                new PortalServiceCard("Okerry Community", "서비스 공지와 사용자 소식을 확인하는 커뮤니티 허브입니다.", "/portal/services"),
                new PortalServiceCard("Okerry Community", "서비스 공지와 사용자 소식을 확인하는 커뮤니티 허브입니다.", "/portal/services"),
                new PortalServiceCard("Okerry Community", "서비스 공지와 사용자 소식을 확인하는 커뮤니티 허브입니다.", "/portal/services"),
                new PortalServiceCard("Okerry Community", "서비스 공지와 사용자 소식을 확인하는 커뮤니티 허브입니다.", "/portal/services"),
                new PortalServiceCard("Okerry Community", "서비스 공지와 사용자 소식을 확인하는 커뮤니티 허브입니다.", "/portal/services"),
                new PortalServiceCard("Okerry Community", "서비스 공지와 사용자 소식을 확인하는 커뮤니티 허브입니다.", "/portal/services"),
                new PortalServiceCard("Okerry Community", "서비스 공지와 사용자 소식을 확인하는 커뮤니티 허브입니다.", "/portal/services"),
                new PortalServiceCard("Okerry Community", "서비스 공지와 사용자 소식을 확인하는 커뮤니티 허브입니다.", "/portal/services"),
                new PortalServiceCard("Okerry Community", "서비스 공지와 사용자 소식을 확인하는 커뮤니티 허브입니다.", "/portal/services"),
                new PortalServiceCard("Okerry Community", "서비스 공지와 사용자 소식을 확인하는 커뮤니티 허브입니다.", "/portal/services"),
                new PortalServiceCard("Okerry Community", "서비스 공지와 사용자 소식을 확인하는 커뮤니티 허브입니다.", "/portal/services"),
                new PortalServiceCard("Okerry Assets", "공용 리소스와 파일을 정리하는 에셋 저장소입니다.", "/portal/services")
        );
    }

    private record PortalServiceCard(
            String name,
            String description,
            String entryPoint
    ) {
    }
}
