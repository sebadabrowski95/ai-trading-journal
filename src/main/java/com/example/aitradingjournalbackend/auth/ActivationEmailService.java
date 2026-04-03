package com.example.aitradingjournalbackend.auth;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivationEmailService {

    private final JavaMailSender mailSender;
    private final ActivationProperties activationProperties;
    private final TemplateEngine templateEngine;

    public void sendActivationEmail(String toEmail, String token) {
        String activationLink = activationProperties.baseUrl() + "/activate-account#token=" + token;
        try {
            Context context = new Context();
            context.setVariable("activationUrl", activationLink);
            String htmlBody = templateEngine.process("email/activation-email", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(activationProperties.fromEmail());
            helper.setTo(toEmail);
            helper.setSubject("Aktywacja konta - AI Trading Journal");
            helper.setText(htmlBody, true);
            mailSender.send(message);
        } catch (Exception ex) {
            log.error("Nie udało się wysłać maila aktywacyjnego do {}", toEmail, ex);
            throw new IllegalStateException("Wysyłka maila aktywacyjnego nie powiodła się", ex);
        }
    }
}
