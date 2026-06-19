package com.employeehub.employeehub.shared.exception;

public class EmailAlreadyUsedException extends RuntimeException {
    public EmailAlreadyUsedException() {
        super("Email already in use");
    }
}
