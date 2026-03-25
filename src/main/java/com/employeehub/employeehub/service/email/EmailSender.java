package com.employeehub.employeehub.service.email;

import java.util.UUID;

public interface EmailSender {

    void send(String to, EmailTemplate template, UUID token);

}