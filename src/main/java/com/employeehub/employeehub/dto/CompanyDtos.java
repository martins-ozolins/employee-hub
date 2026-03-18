package com.employeehub.employeehub.dto;

import java.time.Instant;
import java.util.UUID;

public class CompanyDtos {

    public record CreateCompanyDto(
            String name,
            String industry,
            String location,
            String description
    ) {}

    public record CompanyResponseDto(
            UUID id,
            String name,
            String industry,
            String location,
            String description,
            Instant createdAt
    ) {}

}
