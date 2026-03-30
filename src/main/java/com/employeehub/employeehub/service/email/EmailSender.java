package com.employeehub.employeehub.service.email;

public interface EmailSender {

    void send(String to, EmailTemplate template);

}