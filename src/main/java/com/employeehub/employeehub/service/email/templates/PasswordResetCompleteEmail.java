package com.employeehub.employeehub.service.email.templates;

import com.employeehub.employeehub.service.email.EmailTemplate;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class PasswordResetCompleteEmail implements EmailTemplate {

    private final String timestamp;

    public PasswordResetCompleteEmail() {
        this.timestamp = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                .withZone(ZoneOffset.UTC)
                .format(Instant.now());
    }

    @Override
    public String getSubject() {
        return "Your password has been reset";
    }

    @Override
    public String getBody() {
        return "Your password was successfully reset on " + timestamp + " UTC.\n\n"
                + "If you did not request this change, please contact support immediately.";
    }

}