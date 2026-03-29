package com.employeehub.employeehub.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.UUID;

public class CompanyDtos {

    public record CreateCompanyDto(
            @NotBlank String name,
            @NotBlank String industry,
            @NotBlank String location,
            @NotBlank String description
    ) {}

    public record CompanyResponseDto(
            UUID id,
            String name,
            String industry,
            String location,
            String description,
            Instant createdAt
    ) {}

    public record CompanyBasicDto(
            UUID id,
            String name,
            String industry,
            Instant createdAt
    ) {}

    public record UpdateCompanyDto(
            @NotBlank String name,
            @NotBlank String industry,
            @NotBlank String location,
            @NotBlank String description
    ) {}

}
