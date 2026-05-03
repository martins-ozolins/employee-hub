package com.employeehub.employeehub.event;

import com.employeehub.employeehub.config.RabbitMqConfig;
import com.employeehub.employeehub.service.email.EmailSender;
import com.employeehub.employeehub.service.email.EmailTemplate;
import com.employeehub.employeehub.service.email.templates.EmailVerificationEmail;
import com.employeehub.employeehub.service.email.templates.PasswordChangedEmail;
import com.employeehub.employeehub.service.email.templates.PasswordResetCompleteEmail;
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
        };

        emailSender.send(event.getRecipientEmail(), template);
    }
}
