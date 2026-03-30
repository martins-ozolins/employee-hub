package com.employeehub.employeehub.service.email.templates;

import com.employeehub.employeehub.service.email.EmailTemplate;

import java.util.UUID;

public class EmailVerificationEmail implements EmailTemplate {

    private final String verificationLink;

    public EmailVerificationEmail(String baseUrl, UUID token) {
        this.verificationLink = baseUrl + "/auth/verify-email?token=" + token;
    }

    @Override
    public String getSubject() {
        return "Verify your email";
    }

    @Override
    public String getBody() {
        return "Click the link to verify your email: " + verificationLink;
    }

}