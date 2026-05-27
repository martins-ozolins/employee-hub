package com.employeehub.employeehub.event;

import com.employeehub.employeehub.config.RabbitMqConfig;
import com.employeehub.employeehub.service.email.EmailSender;
import com.employeehub.employeehub.service.email.EmailTemplate;
import com.employeehub.employeehub.service.email.templates.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class EmailEventListener {

    private static final Logger log = LoggerFactory.getLogger(EmailEventListener.class);

    private final EmailSender emailSender;

    public EmailEventListener(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    @RabbitListener(queues = RabbitMqConfig.EMAIL_QUEUE)
    public void handleEmailEvent(EmailEvent event) {
        log.info("Received email event: type={}, recipient={}", event.getType(), event.getRecipientEmail());

        EmailTemplate template = switch (event.getType()) {
            case EMAIL_VERIFICATION -> new EmailVerificationEmail(
                    event.getData().get("baseUrl"),
                    UUID.fromString(event.getData().get("token"))
            );
            case PASSWORD_CHANGED -> new PasswordChangedEmail();
            case PASSWORD_RESET -> new PasswordResetCompleteEmail();
            case PASSWORD_RESET_REQUESTED -> new PasswordResetEmail(
                    event.getData().get("baseUrl"),
                    UUID.fromString(event.getData().get("token"))
            );
            case DOCUMENT_EXPIRY_HR -> new PrerenderedEmail(event.getData().get("subject"), event.getData().get("body"));
        };

        emailSender.send(event.getRecipientEmail(), template);
    }
}
