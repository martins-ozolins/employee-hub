package com.employeehub.employeehub.features.email.template;

import com.employeehub.employeehub.features.email.service.EmailTemplate;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class PasswordChangedEmail implements EmailTemplate {

    private final String timestamp;

    public PasswordChangedEmail() {
        this.timestamp = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                .withZone(ZoneOffset.UTC)
                .format(Instant.now());
    }

    @Override
    public String getSubject() {
        return "Your password has been changed";
    }

    @Override
    public String getBody() {
        return "Your password was successfully changed on " + timestamp + " UTC.\n\n"
                + "If you did not make this change, please reset your password immediately or contact support.";
    }

}
