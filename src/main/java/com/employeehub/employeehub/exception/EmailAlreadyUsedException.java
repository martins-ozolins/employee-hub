package com.employeehub.employeehub.exception;



public class EmailAlreadyUsedException extends RuntimeException {
    public EmailAlreadyUsedException() {
        super("Email already in use");
    }
}
