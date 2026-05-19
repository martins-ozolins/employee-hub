package com.employeehub.employeehub.service;

import com.employeehub.employeehub.config.AppUserDetails;
import com.employeehub.employeehub.dto.CompanyRoleDtos.*;
import com.employeehub.employeehub.entity.CompanyMember;
import com.employeehub.employeehub.entity.CompanyPermissionEntity;
import com.employeehub.employeehub.entity.CompanyRoleEntity;
import com.employeehub.employeehub.entity.Permission;
import com.employeehub.employeehub.exception.BadRequestException;
import com.employeehub.employeehub.exception.ConflictException;
import com.employeehub.employeehub.exception.ForbiddenException;
import com.employeehub.employeehub.exception.NotFoundException;
import com.employeehub.employeehub.repository.CompanyMemberRepository;
import com.employeehub.employeehub.repository.CompanyPermissionRepository;
import com.employeehub.employeehub.repository.CompanyRoleRepository;
import com.employeehub.employeehub.util.CompanyRoleUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class CompanyRoleService {

    private final CompanyRoleRepository companyRoleRepository;
    private final CompanyPermissionRepository companyPermissionRepository;
    private final CompanyMemberRepository companyMemberRepository;
    private final PermissionService permissionService;

    public CompanyRoleService(CompanyRoleRepository companyRoleRepository, CompanyPermissionRepository companyPermissionRepository, CompanyMemberRepository companyMemberRepository, PermissionService permissionService) {
        this.companyRoleRepository = companyRoleRepository;
        this.companyPermissionRepository = companyPermissionRepository;
        this.companyMemberRepository = companyMemberRepository;
        this.permissionService = permissionService;
    }

    public List<RoleResponseDto> getCompanyRoles(UUID companyId, AppUserDetails principal) {
        CompanyMember caller = permissionService.getCallerOrThrow(principal, companyId);
        permissionService.checkPermission(caller, Permission.VIEW_MEMBERS);

        return companyRoleRepository.findByCompanyId(companyId).stream()
                .map(CompanyRoleUtils::toResponseDto)
                .toList();
    }

    @Transactional
    public RoleResponseDto createRole(UUID companyId, AppUserDetails principal, CreateRoleDto dto) {
        CompanyMember caller = permissionService.getCallerOrThrow(principal, companyId);
        permissionService.checkPermission(caller, Permission.MANAGE_COMPANY);

        if (companyRoleRepository.existsByCompanyIdAndName(companyId, dto.name())) {
            throw new ConflictException("A role with that name already exists in this company");
        }

        Set<CompanyPermissionEntity> permissions = new HashSet<>(
                companyPermissionRepository.findAllById(dto.permissionIds())
        );

        CompanyRoleEntity role = CompanyRoleEntity.builder()
                .company(caller.getCompany())
                .name(dto.name())
                .isSystem(false)
                .permissions(permissions)
                .build();

        return CompanyRoleUtils.toResponseDto(companyRoleRepository.save(role));
    }

    @Transactional
    public RoleResponseDto updateRole(UUID companyId, UUID roleId, AppUserDetails principal, UpdateRoleDto dto) {
        CompanyMember caller = permissionService.getCallerOrThrow(principal, companyId);
        permissionService.checkPermission(caller, Permission.MANAGE_COMPANY);

        CompanyRoleEntity role = companyRoleRepository.findByCompanyIdAndId(companyId, roleId)
                .orElseThrow(() -> new NotFoundException("Role not found"));

        if (role.isSystem()) {
            throw new ForbiddenException("Cannot update a system role");
        }

        if (!role.getName().equals(dto.name()) && companyRoleRepository.existsByCompanyIdAndName(companyId, dto.name())) {
            throw new ConflictException("A role with that name already exists in this company");
        }

        role.setName(dto.name());
        role.setPermissions(new HashSet<>(companyPermissionRepository.findAllById(dto.permissionIds())));

        return CompanyRoleUtils.toResponseDto(companyRoleRepository.save(role));
    }

    @Transactional
    public void deleteRole(UUID companyId, UUID roleId, AppUserDetails principal) {
        CompanyMember caller = permissionService.getCallerOrThrow(principal, companyId);
        permissionService.checkPermission(caller, Permission.MANAGE_COMPANY);

        CompanyRoleEntity role = companyRoleRepository.findByCompanyIdAndId(companyId, roleId)
                .orElseThrow(() -> new NotFoundException("Role not found"));

        if (role.isSystem()) {
            throw new ForbiddenException("Cannot delete a system role");
        }

        if (companyMemberRepository.countByCompanyIdAndCompanyRole(companyId, role) > 0) {
            throw new BadRequestException("Cannot delete a role that is assigned to members");
        }

        companyRoleRepository.delete(role);
    }

}
