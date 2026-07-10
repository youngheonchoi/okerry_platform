package com.okerry.platform.serviceaccess;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

@Service
@Profile("neon")
public class ServiceAccessRequestMailService {

    private final ObjectProvider<MailSender> mailSenderProvider;
    private final String sender;
    private final String adminEmail;

    public ServiceAccessRequestMailService(
            ObjectProvider<MailSender> mailSenderProvider,
            @Value("${platform.mail.sender:gjswns21@gmail.com}") String sender,
            @Value("${platform.service-access-request.admin-email:gjswns21@gmail.com}") String adminEmail) {
        this.mailSenderProvider = mailSenderProvider;
        this.sender = sender;
        this.adminEmail = adminEmail;
    }

    public void sendRequest(PendingServiceAccessRequest request) {
        MailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            throw new IllegalStateException("MailSender is not available");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(sender);
        message.setTo(adminEmail);
        message.setSubject("[okerry platform] 서비스 접근 요청 - " + request.serviceName());
        message.setText("""
                서비스 접근 요청이 접수되었습니다.

                사용자 이메일: %s
                사용자 이름: %s
                요청 서비스: %s
                서비스 키: %s
                OAuth2 Client ID: %s
                요청 시각: %s

                해당 사용자는 현재 서비스 접근 권한을 보유하지 않아 Authorization Code 발급이 차단되었습니다.
                Console에서 사용자별 접근 가능 서비스를 확인한 뒤 필요한 경우 직접 등록해주세요.
                """.formatted(
                request.userEmail(),
                request.userName(),
                request.serviceName(),
                request.serviceKey(),
                request.oauth2ClientId(),
                request.requestedAt()));
        mailSender.send(message);
    }
}
