package com.employeehub.employeehub.service.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Profile("!local")
public class SmtpEmailService implements EmailSender {

    private final JavaMailSender mailSender;

    @Value("${app.baseUrl}")
    private String baseUrl;

    public SmtpEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void send(String to, EmailTemplate template, UUID token) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject(template.getSubject());
        msg.setText(template.getBody(baseUrl, token.toString()));
        msg.setFrom("noreply@example.com");

        mailSender.send(msg);
    }

}