package com.employeehub.employeehub.dto;

import com.employeehub.employeehub.entity.TitleChangeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class JobTitleDtos {

    public record AddJobTitleDto(
            @NotBlank @Size(max = 200) String jobTitle,
            @NotNull TitleChangeType changeType,
            @NotNull LocalDate effectiveDate,
            @Size(max = 500) String notes
    ) {}

    public record JobTitleRecordDto(
            UUID id,
            String jobTitle,
            TitleChangeType changeType,
            LocalDate effectiveDate,
            String notes,
            Instant createdAt
    ) {}
}