package com.employeehub.employeehub.features.documents.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class DocumentDtos {

    public record DocumentDto(
            UUID id,
            String fileName,
            String contentType,
            Long fileSize,
            LocalDate expiryDate,
            Instant uploadedAt
    ) {}

    public record DocumentDownloadDto(
            UUID id,
            String fileName,
            String downloadUrl
    ) {}
}
