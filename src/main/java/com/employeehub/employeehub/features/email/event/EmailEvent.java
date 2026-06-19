package com.employeehub.employeehub.features.email.event;

import java.util.Map;

public class EmailEvent {

    private EmailEventType type;
    private String recipientEmail;
    private Map<String, String> data;

    public EmailEvent() {
    }

    public EmailEvent(EmailEventType type, String recipientEmail, Map<String, String> data) {
        this.type = type;
        this.recipientEmail = recipientEmail;
        this.data = data;
    }

    public EmailEventType getType() {
        return type;
    }

    public void setType(EmailEventType type) {
        this.type = type;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }
}
