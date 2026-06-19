package com.employeehub.employeehub.shared.exception;

public class EmailNotVerifiedException extends RuntimeException {
    public EmailNotVerifiedException() {
        super("Email not verified. A new verification link has been sent to your email.");
    }
}
