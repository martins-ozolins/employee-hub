package com.employeehub.employeehub.features.roles.service;

import com.employeehub.employeehub.features.members.entity.CompanyMember;
import com.employeehub.employeehub.features.members.entity.EmploymentStatus;
import com.employeehub.employeehub.features.members.repository.CompanyMemberRepository;
import com.employeehub.employeehub.features.roles.entity.CompanyPermission;
import com.employeehub.employeehub.security.model.AppUserDetails;
import com.employeehub.employeehub.shared.exception.ForbiddenException;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CompanyPermissionService {

    private final CompanyMemberRepository companyMemberRepository;

    public CompanyPermissionService(CompanyMemberRepository companyMemberRepository) {
        this.companyMemberRepository = companyMemberRepository;
    }

    public CompanyMember getCallerOrThrow(AppUserDetails principal, UUID companyId) {
        return companyMemberRepository
                .findByUserIdAndCompanyId(principal.getId(), companyId)
                .orElseThrow(() -> new ForbiddenException("Access denied"));
    }

    public boolean hasPermission(CompanyMember caller, CompanyPermission permission) {
        return caller.getCompanyRole().getPermissions().stream()
                .anyMatch(p -> p.getName().equals(permission.name()));
    }

    public void checkPermission(CompanyMember caller, CompanyPermission permission) {
        if (!hasPermission(caller, permission)) {
            throw new ForbiddenException("Access denied");
        }
    }

    public Set<CompanyPermission> getPermissions(CompanyMember caller) {
        return caller.getCompanyRole().getPermissions().stream()
                .map(p -> CompanyPermission.valueOf(p.getName()))
                .collect(Collectors.toSet());
    }

    public void checkSelfServiceAccess(CompanyMember caller) {
        if (!Boolean.TRUE.equals(caller.getSelfServiceEnabled())
                || caller.getEmploymentStatus() == EmploymentStatus.TERMINATED
                || caller.getEmploymentStatus() == EmploymentStatus.SUSPENDED) {
            throw new ForbiddenException("Access denied");
        }
    }
}
