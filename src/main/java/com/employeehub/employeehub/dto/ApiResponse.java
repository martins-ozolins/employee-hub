package com.employeehub.employeehub.dto;

public record ApiResponse<T>(String message, T data) {

    public ApiResponse(String message) {
        this(message, null);
    }

}
