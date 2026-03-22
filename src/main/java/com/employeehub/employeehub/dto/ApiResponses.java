package com.employeehub.employeehub.dto;

import java.util.List;

public class ApiResponses {

    public record MessageResponse(String message) {}

    public record DataResponse<T>(T data) {}

    public record MessageAndDataResponse<T>(String message, T data) {}

    public record PageMeta(
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean hasNext,
            boolean hasPrevious
    ) {}

    public record PagedDataResponse<T>(
            List<T> data,
            PageMeta meta
    ) {}

}