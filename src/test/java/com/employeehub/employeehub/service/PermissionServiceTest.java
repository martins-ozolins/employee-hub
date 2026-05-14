package com.employeehub.employeehub.service;


import com.employeehub.employeehub.config.AppUserDetails;
import com.employeehub.employeehub.entity.*;
import com.employeehub.employeehub.exception.ForbiddenException;
import com.employeehub.employeehub.repository.CompanyMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class PermissionServiceTest {

    @Mock
    CompanyMemberRepository companyMemberRepository;

    PermissionService permissionService;

    @BeforeEach
    void setup() {

        permissionService = new PermissionService(companyMemberRepository);

    }

    @Test
    void getCallerOrThrow_returnsMemberWhenFound() {
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();

        AppUserDetails principal = new AppUserDetails(userId, "user@test.com", null, List.of());

        CompanyMember member = CompanyMember.builder()
                .role(CompanyRole.HR)
                .build();

        when(companyMemberRepository.findByUserIdAndCompanyId(userId, companyId))
                .thenReturn(Optional.of(member));

        CompanyMember result = permissionService.getCallerOrThrow(principal, companyId);

        assertThat(result).isEqualTo(member);
    }

    @Test
    void getCallerOrThrow_throwsForbiddenWhenNotMember() {
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();

        AppUserDetails principal = new AppUserDetails(userId, "user@test.com", null, List.of());

        when(companyMemberRepository.findByUserIdAndCompanyId(userId, companyId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> permissionService.getCallerOrThrow(principal, companyId))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void hasPermission_whenRoleHasPermission_returnsTrue() {

        CompanyMember member = CompanyMember.builder()
                .role(CompanyRole.HR)
                .build();

        assertThat(permissionService.hasPermission(member, Permission.VIEW_MEMBERS)).isTrue();


    }

    @Test
    void hasPermission_whenRoleDoesNotHavePermission_returnsFalse() {

        CompanyMember member = CompanyMember.builder()
                .role(CompanyRole.EMPLOYEE)
                .build();

        assertThat(permissionService.hasPermission(member, Permission.MANAGE_MEMBERS)).isFalse();

    }

    @Test
    void checkPermission_whenRoleLacksPermission_throwsForbidden() {

        CompanyMember member = CompanyMember.builder()
                .role(CompanyRole.MANAGER)
                .build();

        assertThatThrownBy(() -> permissionService.checkPermission(member, Permission.MANAGE_SALARY))
                .isInstanceOf(ForbiddenException.class);

    }

    @Test
    void getPermissions_returnsCorrectSetForRole() {

        CompanyMember member = CompanyMember.builder()
                .role(CompanyRole.HR)
                .build();

        Set<Permission> permissions = permissionService.getPermissions(member);

        assertThat(permissions).contains(
                Permission.VIEW_MEMBERS,
                Permission.VIEW_MEMBER_DETAILS,
                Permission.MANAGE_MEMBERS,
                Permission.MANAGE_SALARY,
                Permission.MANAGE_JOB_TITLES,
                Permission.MANAGE_DOCUMENTS
        );
        assertThat(permissions).doesNotContain(Permission.MANAGE_COMPANY);

    }

    @Test
    void checkSelfServiceAccess_whenSelfServiceDisabled_throwsForbidden() {

        CompanyMember member = CompanyMember.builder()
                .selfServiceEnabled(false)
                .employmentStatus(EmploymentStatus.ACTIVE)
                .build();

        assertThatThrownBy(() -> permissionService.checkSelfServiceAccess(member))
                .isInstanceOf(ForbiddenException.class);

    }

}
