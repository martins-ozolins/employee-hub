package com.employeehub.employeehub.util;

import com.employeehub.employeehub.dto.CompanyMemberDtos.MemberDetailDto;
import com.employeehub.employeehub.dto.CompanyMemberDtos.MemberDirectoryDto;
import com.employeehub.employeehub.dto.CompanyMemberDtos.MemberSelfResponseDto;
import com.employeehub.employeehub.dto.CompanyMemberDtos.MemberSummaryDto;
import com.employeehub.employeehub.entity.CompanyMember;
import com.employeehub.employeehub.entity.Permission;

import java.util.Set;

public class CompanyMemberUtils {

    private CompanyMemberUtils() {}

    public static MemberSummaryDto toSummaryDto(CompanyMember m) {
        return new MemberSummaryDto(
                m.getId(),
                m.getUser() != null ? m.getUser().getId() : null,
                m.getFirstName(),
                m.getMiddleName(),
                m.getLastName(),
                m.getRole(),
                m.getEmploymentStatus(),
                m.getJobTitle(),
                m.getDepartment(),
                m.getWorkEmail(),
                m.getCreatedAt()
        );
    }

    public static MemberDirectoryDto toDirectoryDto(CompanyMember m) {
        return new MemberDirectoryDto(
                m.getId(),
                m.getFirstName(),
                m.getLastName(),
                m.getRole(),
                m.getDepartment(),
                m.getJobTitle(),
                m.getWorkEmail()
        );
    }

    public static MemberSelfResponseDto toSelfDto(CompanyMember m, Set<Permission> permissions) {
        return new MemberSelfResponseDto(
                m.getId(),
                m.getFirstName(),
                m.getLastName(),
                m.getMiddleName(),
                m.getJobTitle(),
                m.getDepartment(),
                m.getJoinDate(),
                m.getWorkEmail(),
                m.getPersonalEmail(),
                m.getPhoneNumber(),
                m.getDateOfBirth(),
                m.getAddress(),
                m.getPersonalCode(),
                m.getBankAccount(),
                m.getEmergencyContactName(),
                m.getEmergencyContactPhone(),
                permissions
        );
    }

    public static MemberDetailDto toDetailDto(CompanyMember m) {
        return new MemberDetailDto(
                m.getId(),
                m.getUser() != null ? m.getUser().getId() : null,
                m.getFirstName(),
                m.getLastName(),
                m.getMiddleName(),
                m.getRole(),
                m.getEmploymentStatus(),
                m.getSelfServiceEnabled(),
                m.getJobTitle(),
                m.getDepartment(),
                m.getJoinDate(),
                m.getWorkEmail(),
                m.getPersonalEmail(),
                m.getPhoneNumber(),
                m.getDateOfBirth(),
                m.getAddress(),
                m.getPersonalCode(),
                m.getBankAccount(),
                m.getEmergencyContactName(),
                m.getEmergencyContactPhone(),
                m.getCurrentSalaryAmount(),
                m.getCurrentSalaryCurrency(),
                m.getCreatedAt()
        );
    }
}