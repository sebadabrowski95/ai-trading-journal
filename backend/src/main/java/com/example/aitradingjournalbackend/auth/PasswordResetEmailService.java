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
public class PasswordResetEmailService {

    private final JavaMailSender mailSender;
    private final PasswordResetProperties passwordResetProperties;
    private final TemplateEngine templateEngine;

    public void sendPasswordResetEmail(String toEmail, String token) {
        String resetLink = passwordResetProperties.baseUrl() + "/reset-password#token=" + token;
        try {
            Context context = new Context();
            context.setVariable("resetUrl", resetLink);
            String htmlBody = templateEngine.process("email/password-reset-email", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(passwordResetProperties.fromEmail());
            helper.setTo(toEmail);
            helper.setSubject("Reset hasła - AI Trading Journal");
            helper.setText(htmlBody, true);
            mailSender.send(message);
        } catch (Exception ex) {
            log.error("Nie udało się wysłać maila resetującego hasło do {}", toEmail, ex);
            throw new IllegalStateException("Wysyłka maila resetującego hasło nie powiodła się", ex);
        }
    }
}
