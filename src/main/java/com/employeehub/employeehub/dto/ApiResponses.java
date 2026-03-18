package com.employeehub.employeehub.dto;

public class ApiResponses {

    public record MessageResponse(String message) {}

    public record DataResponse<T>(T data) {}

    public record MessageAndDataResponse<T>(String message, T data) {}

}