package com.employeehub.employeehub.features.members.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class SalaryDtos {

    public record AddSalaryDto(
            @NotNull @Positive BigDecimal amount,
            @NotBlank @Size(max = 3) String currency,
            @NotNull LocalDate effectiveDate,
            @Size(max = 500) String notes
    ) {}

    public record SalaryRecordDto(
            UUID id,
            BigDecimal amount,
            String currency,
            LocalDate effectiveDate,
            String notes,
            Instant createdAt
    ) {}
}
