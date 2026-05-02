package com.employeehub.employeehub.service.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("local")
public class LogEmailService implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(LogEmailService.class);

    @Override
    public void send(String to, EmailTemplate template) {
        log.info("""
                MOCK EMAIL
                To: {}
                Subject: {}
                Body:
                {}
                """, to, template.getSubject(), template.getBody());
    }

}