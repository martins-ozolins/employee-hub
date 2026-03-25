package com.employeehub.employeehub.exception;

public class EmailNotVerifiedException extends RuntimeException {
    public EmailNotVerifiedException() {
        super("Email not verified. A new verification link has been sent to your email.");
    }
}