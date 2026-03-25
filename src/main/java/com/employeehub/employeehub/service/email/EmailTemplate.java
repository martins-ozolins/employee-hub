package com.employeehub.employeehub.service.email;

import com.employeehub.employeehub.entity.TokenType;

public enum EmailTemplate {

    EMAIL_VERIFICATION(
            "Verify your email",
            "Click the link to verify your email: %s",
            "/auth/verify-email?token="
    ),
    PASSWORD_RESET(
            "Reset your password",
            "Click the link to reset your password: %s",
            "/auth/reset-password?token="
    );

    private final String subject;
    private final String bodyTemplate;
    private final String path;

    EmailTemplate(String subject, String bodyTemplate, String path) {
        this.subject = subject;
        this.bodyTemplate = bodyTemplate;
        this.path = path;
    }

    public static EmailTemplate from(TokenType type) {
        return switch (type) {
            case EMAIL_VERIFICATION -> EMAIL_VERIFICATION;
            case PASSWORD_RESET -> PASSWORD_RESET;
        };
    }

    public String getSubject() {
        return subject;
    }

    public String getBody(String baseUrl, String token) {
        return String.format(bodyTemplate, baseUrl + path + token);
    }

}