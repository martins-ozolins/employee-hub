package com.employeehub.employeehub.util;

import com.employeehub.employeehub.dto.CompanyRoleDtos.CompanyPermissionDto;
import com.employeehub.employeehub.dto.CompanyRoleDtos.RoleResponseDto;
import com.employeehub.employeehub.entity.CompanyRoleEntity;

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