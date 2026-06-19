package com.employeehub.employeehub.features.email.template;

import com.employeehub.employeehub.features.email.service.EmailTemplate;

public class PrerenderedEmail implements EmailTemplate {

    private final String subject;
    private final String body;

    public PrerenderedEmail(String subject, String body) {
        this.subject = subject;
        this.body = body;
    }

    @Override
    public String getSubject() {
        return subject;
    }

    @Override
    public String getBody() {
        return body;
    }
}
