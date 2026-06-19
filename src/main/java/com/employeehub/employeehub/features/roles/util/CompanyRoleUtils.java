package com.employeehub.employeehub.features.roles.util;

import com.employeehub.employeehub.features.roles.dto.CompanyRoleDtos.CompanyPermissionDto;
import com.employeehub.employeehub.features.roles.dto.CompanyRoleDtos.RoleResponseDto;
import com.employeehub.employeehub.features.roles.entity.CompanyRoleEntity;

import java.util.Set;
import java.util.stream.Collectors;

public class CompanyRoleUtils {

    private CompanyRoleUtils() {}

    public static RoleResponseDto toResponseDto(CompanyRoleEntity role) {
        Set<CompanyPermissionDto> permissionDtos = role.getPermissions().stream()
                .map(p -> new CompanyPermissionDto(p.getId(), p.getName(), p.getDescription()))
                .collect(Collectors.toSet());

        return RoleResponseDto.builder()
                .id(role.getId())
                .name(role.getName())
                .isSystem(role.isSystem())
                .permissions(permissionDtos)
                .createdAt(role.getCreatedAt())
                .build();
    }
}
