package com.employeehub.employeehub.service.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Profile("local")
public class LogEmailService implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(LogEmailService.class);

    @Value("${app.baseUrl}")
    private String baseUrl;

    @Override
    public void send(String to, EmailTemplate template, UUID token) {
        log.info("""
                MOCK EMAIL
                To: {}
                Subject: {}
                Body:
                {}
                """, to, template.getSubject(), template.getBody(baseUrl, token.toString()));
    }

}