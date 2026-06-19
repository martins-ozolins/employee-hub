package com.employeehub.employeehub.features.email.service;

public interface EmailSender {

    void send(String to, EmailTemplate template);

}
