package com.employeehub.employeehub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public class CompanyRoleDtos {

    public record CreateRoleDto(
            @NotBlank @Size(max = 100) String name,
            @NotNull Set<UUID> permissionIds
    ) {}

    public record UpdateRoleDto(
            @NotBlank @Size(max = 100) String name,
            @NotNull Set<UUID> permissionIds
    ) {}

    @Builder
    public record RoleResponseDto(
            UUID id,
            String name,
            boolean isSystem,
            Set<PermissionDto> permissions,
            Instant createdAt
    ) {}

    public record PermissionDto(
            UUID id,
            String name,
            String description
    ) {}

}