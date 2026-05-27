package com.employeehub.employeehub.service.email.templates;

import com.employeehub.employeehub.service.email.EmailTemplate;

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
