package com.employeehub.employeehub.service.email.templates;

import com.employeehub.employeehub.service.email.EmailTemplate;

import java.util.UUID;

public class PasswordResetEmail implements EmailTemplate {

    private final String resetLink;

    public PasswordResetEmail(String baseUrl, UUID token) {
        this.resetLink = baseUrl + "/auth/reset-password?token=" + token;
    }

    @Override
    public String getSubject() {
        return "Reset your password";
    }

    @Override
    public String getBody() {
        return "Click the link to reset your password: " + resetLink;
    }

}