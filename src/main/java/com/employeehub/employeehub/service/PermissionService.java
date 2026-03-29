package com.employeehub.employeehub.service;

import com.employeehub.employeehub.config.AppUserDetails;
import com.employeehub.employeehub.entity.CompanyMember;
import com.employeehub.employeehub.entity.CompanyRole;
import com.employeehub.employeehub.entity.MembershipStatus;
import com.employeehub.employeehub.entity.Permission;
import com.employeehub.employeehub.exception.ForbiddenException;
import com.employeehub.employeehub.repository.CompanyMemberRepository;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class PermissionService {

    private final CompanyMemberRepository companyMemberRepository;

    private static final Map<CompanyRole, Set<Permission>> ROLE_PERMISSIONS = new EnumMap<>(CompanyRole.class);

    static {
        ROLE_PERMISSIONS.put(CompanyRole.OWNER, Set.of(Permission.values()));

        ROLE_PERMISSIONS.put(CompanyRole.HR, Set.of(
                Permission.VIEW_MEMBERS,
                Permission.VIEW_MEMBER_DETAILS,
                Permission.MANAGE_MEMBERS,
                Permission.MANAGE_SALARY,
                Permission.MANAGE_JOB_TITLES,
                Permission.MANAGE_DOCUMENTS
        ));

        ROLE_PERMISSIONS.put(CompanyRole.MANAGER, Set.of(
                Permission.VIEW_MEMBERS
        ));

        ROLE_PERMISSIONS.put(CompanyRole.EMPLOYEE, Set.of(
                Permission.VIEW_MEMBERS
        ));
    }

    public PermissionService(CompanyMemberRepository companyMemberRepository) {
        this.companyMemberRepository = companyMemberRepository;
    }

    public CompanyMember getCallerOrThrow(AppUserDetails principal, UUID companyId) {
        return companyMemberRepository
                .findByUserIdAndCompanyId(principal.getId(), companyId)
                .orElseThrow(() -> new ForbiddenException("Access denied"));
    }

    public boolean hasPermission(CompanyMember caller, Permission permission) {
        Set<Permission> permissions = ROLE_PERMISSIONS.getOrDefault(caller.getRole(), Set.of());
        return permissions.contains(permission);
    }

    public void checkPermission(CompanyMember caller, Permission permission) {
        if (!hasPermission(caller, permission)) {
            throw new ForbiddenException("Access denied");
        }
    }

    public Set<Permission> getPermissions(CompanyMember caller) {
        return ROLE_PERMISSIONS.getOrDefault(caller.getRole(), Set.of());
    }

    public void checkSelfServiceAccess(CompanyMember caller) {
        if (!Boolean.TRUE.equals(caller.getSelfServiceEnabled())
                || caller.getMembershipStatus() != MembershipStatus.ACTIVE) {
            throw new ForbiddenException("Access denied");
        }
    }
}
