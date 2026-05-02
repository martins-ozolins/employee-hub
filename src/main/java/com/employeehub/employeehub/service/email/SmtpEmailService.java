package com.employeehub.employeehub.service.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Profile("!local")
public class SmtpEmailService implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(SmtpEmailService.class);

    private final JavaMailSender mailSender;

    public SmtpEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void send(String to, EmailTemplate template) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(to);
            msg.setSubject(template.getSubject());
            msg.setText(template.getBody());
            msg.setFrom("noreply@example.com");

            mailSender.send(msg);
            log.info("Email sent to {} [subject={}]", to, template.getSubject());
        } catch (MailException e) {
            log.error("Failed to send email to {} [subject={}]: {}", to, template.getSubject(), e.getMessage());
        }
    }

}