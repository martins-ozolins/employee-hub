package com.employeehub.employeehub.event;

import com.employeehub.employeehub.config.RabbitMqConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class EmailEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EmailEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public EmailEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(EmailEvent event) {
        log.info("Publishing email event: type={}, recipient={}", event.getType(), event.getRecipientEmail());
        rabbitTemplate.convertAndSend(RabbitMqConfig.EMAIL_EXCHANGE, RabbitMqConfig.EMAIL_ROUTING_KEY, event);
    }
}
