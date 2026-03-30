package com.employeehub.employeehub.service.email;

import org.springframework.context.annotation.Profile;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Profile("!local")
public class SmtpEmailService implements EmailSender {

    private final JavaMailSender mailSender;

    public SmtpEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void send(String to, EmailTemplate template) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject(template.getSubject());
        msg.setText(template.getBody());
        msg.setFrom("noreply@example.com");

        mailSender.send(msg);
    }

}