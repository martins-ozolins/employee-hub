package com.employeehub.employeehub.service;

import com.employeehub.employeehub.config.AppUserDetails;
import com.employeehub.employeehub.entity.CompanyMember;
import com.employeehub.employeehub.entity.EmploymentStatus;
import com.employeehub.employeehub.entity.Permission;
import com.employeehub.employeehub.exception.ForbiddenException;
import com.employeehub.employeehub.repository.CompanyMemberRepository;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PermissionService {

    private final CompanyMemberRepository companyMemberRepository;

    public PermissionService(CompanyMemberRepository companyMemberRepository) {
        this.companyMemberRepository = companyMemberRepository;
    }

    public CompanyMember getCallerOrThrow(AppUserDetails principal, UUID companyId) {
        return companyMemberRepository
                .findByUserIdAndCompanyId(principal.getId(), companyId)
                .orElseThrow(() -> new ForbiddenException("Access denied"));
    }

    public boolean hasPermission(CompanyMember caller, Permission permission) {
        return caller.getCompanyRole().getPermissions().stream()
                .anyMatch(p -> p.getName().equals(permission.name()));
    }

    public void checkPermission(CompanyMember caller, Permission permission) {
        if (!hasPermission(caller, permission)) {
            throw new ForbiddenException("Access denied");
        }
    }

    public Set<Permission> getPermissions(CompanyMember caller) {
        return caller.getCompanyRole().getPermissions().stream()
                .map(p -> Permission.valueOf(p.getName()))
                .collect(Collectors.toSet());
    }

    public void checkSelfServiceAccess(CompanyMember caller) {
        if (!Boolean.TRUE.equals(caller.getSelfServiceEnabled())
                || caller.getEmploymentStatus() == EmploymentStatus.TERMINATED) {
            throw new ForbiddenException("Access denied");
        }
    }
}