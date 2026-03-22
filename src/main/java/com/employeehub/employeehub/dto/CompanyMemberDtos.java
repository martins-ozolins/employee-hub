package com.employeehub.employeehub.dto;

import com.employeehub.employeehub.entity.CompanyRole;
import com.employeehub.employeehub.entity.EmploymentStatus;
import com.employeehub.employeehub.entity.MembershipStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class CompanyMemberDtos {

    // HR/OWNER creates a new member
    public record CreateMemberDto(
            @NotBlank @Size(max = 100) String firstName,
            @NotBlank @Size(max = 100) String lastName,
            @Size(max = 100) String middleName,
            @NotBlank @Email @Size(max = 255) String personalEmail,
            @NotNull CompanyRole role,
            @NotNull Boolean selfServiceEnabled,
            @Size(max = 255) String jobTitle,
            @NotBlank @Size(max = 100) String department,
            @NotNull LocalDate joinDate,
            @Email @Size(max = 255) String workEmail,
            @Size(max = 50) String phoneNumber,
            @Past LocalDate dateOfBirth,
            @Size(max = 500) String address,
            @Size(max = 100) String personalCode,
            @Size(max = 100) String bankAccount,
            @Size(max = 255) String emergencyContactName,
            @Size(max = 50) String emergencyContactPhone,
            @Positive BigDecimal initialSalaryAmount,
            @Size(min = 3, max = 3) String initialSalaryCurrency
    ) {}

    // HR/OWNER updates any member
    public record UpdateMemberDto(
            @NotBlank @Size(max = 100) String firstName,
            @NotBlank @Size(max = 100) String lastName,
            @Size(max = 100) String middleName,
            @NotBlank @Email @Size(max = 255) String personalEmail,
            @NotNull CompanyRole role,
            @NotNull MembershipStatus membershipStatus,
            @NotNull EmploymentStatus employmentStatus,
            @NotNull Boolean selfServiceEnabled,
            @NotBlank @Size(max = 100) String department,
            @NotNull LocalDate joinDate,
            @Email @Size(max = 255) String workEmail,
            @Size(max = 50) String phoneNumber,
            @Past LocalDate dateOfBirth,
            @Size(max = 500) String address,
            @Size(max = 100) String personalCode,
            @Size(max = 100) String bankAccount,
            @Size(max = 255) String emergencyContactName,
            @Size(max = 50) String emergencyContactPhone
    ) {}

    // Lightweight response for list views
    public record MemberSummaryDto(
            UUID id,
            UUID userId,
            String firstName,
            String middleName,
            String lastName,
            CompanyRole role,
            MembershipStatus membershipStatus,
            EmploymentStatus employmentStatus,
            String jobTitle,
            String department,
            String workEmail,
            Instant createdAt
    ) {}

    // Full response for create, update, and get-by-id
    public record MemberDetailDto(
            UUID id,
            UUID userId,
            String firstName,
            String lastName,
            String middleName,
            CompanyRole role,
            MembershipStatus membershipStatus,
            EmploymentStatus employmentStatus,
            Boolean selfServiceEnabled,
            String jobTitle,
            String department,
            LocalDate joinDate,
            String workEmail,
            String personalEmail,
            String phoneNumber,
            LocalDate dateOfBirth,
            String address,
            String personalCode,
            String bankAccount,
            String emergencyContactName,
            String emergencyContactPhone,
            BigDecimal currentSalaryAmount,
            String currentSalaryCurrency,
            Instant createdAt
    ) {}

    // Self-service response for EMPLOYEE (own profile only)
    public record MemberSelfResponseDto(
            UUID id,
            String firstName,
            String lastName,
            String middleName,
            String jobTitle,
            String department,
            LocalDate joinDate,
            String workEmail,
            String personalEmail,
            String phoneNumber,
            LocalDate dateOfBirth,
            String address,
            String personalCode,
            String bankAccount,
            String emergencyContactName,
            String emergencyContactPhone
    ) {}

    // Self-service update — only personal/contact fields
    public record MemberSelfUpdateDto(
            @Size(max = 50) String phoneNumber,
            @Size(max = 500) String address,
            @Size(max = 100) String bankAccount,
            @Size(max = 255) String emergencyContactName,
            @Size(max = 50) String emergencyContactPhone
    ) {}


}