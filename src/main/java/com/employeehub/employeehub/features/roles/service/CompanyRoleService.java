package com.employeehub.employeehub.features.roles.service;

import com.employeehub.employeehub.features.members.entity.CompanyMember;
import com.employeehub.employeehub.features.members.repository.CompanyMemberRepository;
import com.employeehub.employeehub.features.roles.dto.CompanyRoleDtos.*;
import com.employeehub.employeehub.features.roles.entity.CompanyPermission;
import com.employeehub.employeehub.features.roles.entity.CompanyPermissionEntity;
import com.employeehub.employeehub.features.roles.entity.CompanyRoleEntity;
import com.employeehub.employeehub.features.roles.repository.CompanyPermissionRepository;
import com.employeehub.employeehub.features.roles.repository.CompanyRoleRepository;
import com.employeehub.employeehub.features.roles.util.CompanyRoleUtils;
import com.employeehub.employeehub.security.model.AuthenticatedUser;
import com.employeehub.employeehub.shared.dto.ApiResponses.*;
import com.employeehub.employeehub.shared.exception.BadRequestException;
import com.employeehub.employeehub.shared.exception.ConflictException;
import com.employeehub.employeehub.shared.exception.ForbiddenException;
import com.employeehub.employeehub.shared.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class CompanyRoleService {

    private final CompanyRoleRepository companyRoleRepository;
    private final CompanyPermissionRepository companyPermissionRepository;
    private final CompanyMemberRepository companyMemberRepository;
    private final CompanyPermissionService permissionService;

    public CompanyRoleService(CompanyRoleRepository companyRoleRepository, CompanyPermissionRepository companyPermissionRepository, CompanyMemberRepository companyMemberRepository, CompanyPermissionService permissionService) {
        this.companyRoleRepository = companyRoleRepository;
        this.companyPermissionRepository = companyPermissionRepository;
        this.companyMemberRepository = companyMemberRepository;
        this.permissionService = permissionService;
    }

    public PagedDataResponse<RoleResponseDto> getCompanyRoles(UUID companyId, AuthenticatedUser principal, String search, Pageable pageable) {
        CompanyMember caller = permissionService.getCallerOrThrow(principal, companyId);
        permissionService.checkPermission(caller, CompanyPermission.VIEW_MEMBERS);

        Page<CompanyRoleEntity> data = companyRoleRepository.findByCompanyId(companyId, search, pageable);

        return new PagedDataResponse<>(
                data.getContent().stream().map(CompanyRoleUtils::toResponseDto).toList(),
                new PageMeta(data.getNumber(), data.getSize(), data.getTotalElements(), data.getTotalPages(), data.hasNext(), data.hasPrevious())
        );
    }

    @Transactional
    public RoleResponseDto createRole(UUID companyId, AuthenticatedUser principal, CreateRoleDto dto) {
        CompanyMember caller = permissionService.getCallerOrThrow(principal, companyId);
        permissionService.checkPermission(caller, CompanyPermission.MANAGE_COMPANY);

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
    public RoleResponseDto updateRole(UUID companyId, UUID roleId, AuthenticatedUser principal, UpdateRoleDto dto) {
        CompanyMember caller = permissionService.getCallerOrThrow(principal, companyId);
        permissionService.checkPermission(caller, CompanyPermission.MANAGE_COMPANY);

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
    public void deleteRole(UUID companyId, UUID roleId, AuthenticatedUser principal) {
        CompanyMember caller = permissionService.getCallerOrThrow(principal, companyId);
        permissionService.checkPermission(caller, CompanyPermission.MANAGE_COMPANY);

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
