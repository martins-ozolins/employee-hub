package com.employeehub.employeehub.service;

import com.employeehub.employeehub.config.AppUserDetails;
import com.employeehub.employeehub.dto.CompanyMemberDtos.*;
import com.employeehub.employeehub.entity.*;
import com.employeehub.employeehub.exception.ConflictException;
import com.employeehub.employeehub.exception.ForbiddenException;
import com.employeehub.employeehub.repository.CompanyMemberRepository;
import com.employeehub.employeehub.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CompanyMemberService {

    private final CompanyMemberRepository companyMemberRepository;
    private final UserRepository userRepository;

    public CompanyMemberService(
            CompanyMemberRepository companyMemberRepository,
            UserRepository userRepository
    ) {
        this.companyMemberRepository = companyMemberRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public MemberResponseDto create(UUID companyId, AppUserDetails principal, CreateMemberDto dto) {

        CompanyMember caller = companyMemberRepository
                .findMemberByUserIdAndCompanyId(principal.getId(), companyId)
                .orElseThrow(() -> new ForbiddenException("Access denied"));

        if (caller.getRole() != CompanyRole.OWNER && caller.getRole() != CompanyRole.HR) {
            throw new ForbiddenException("Access denied");
        }

        if (companyMemberRepository.existsByPersonalEmailAndCompany(dto.personalEmail(), caller.getCompany())) {
            throw new ConflictException("A member with this email already exists in the company");
        }

        // Auto-link if a user with this email already exists
        User linkedUser = userRepository.findByEmail(dto.personalEmail()).orElse(null);

        CompanyMember newMember = CompanyMember.builder()
                .user(linkedUser)
                .company(caller.getCompany())
                .firstName(dto.firstName())
                .lastName(dto.lastName())
                .middleName(dto.middleName())
                .role(dto.role())
                .membershipStatus(MembershipStatus.ACTIVE)
                .employmentStatus(EmploymentStatus.ACTIVE)
                .selfServiceEnabled(dto.selfServiceEnabled())
                .personalEmail(dto.personalEmail())
                .workEmail(dto.workEmail())
                .phoneNumber(dto.phoneNumber())
                .jobTitle(dto.jobTitle())
                .department(dto.department())
                .joinDate(dto.joinDate())
                .dateOfBirth(dto.dateOfBirth())
                .address(dto.address())
                .personalCode(dto.personalCode())
                .bankAccount(dto.bankAccount())
                .emergencyContactName(dto.emergencyContactName())
                .emergencyContactPhone(dto.emergencyContactPhone())
                .build();

        CompanyMember saved = companyMemberRepository.saveAndFlush(newMember);

        return new MemberResponseDto(
                saved.getId(),
                saved.getUser() != null ? saved.getUser().getId() : null,
                saved.getFirstName(),
                saved.getLastName(),
                saved.getMiddleName(),
                saved.getRole(),
                saved.getMembershipStatus(),
                saved.getEmploymentStatus(),
                saved.getSelfServiceEnabled(),
                saved.getJobTitle(),
                saved.getDepartment(),
                saved.getJoinDate(),
                saved.getWorkEmail(),
                saved.getPersonalEmail(),
                saved.getPhoneNumber(),
                saved.getDateOfBirth(),
                saved.getAddress(),
                saved.getPersonalCode(),
                saved.getBankAccount(),
                saved.getEmergencyContactName(),
                saved.getEmergencyContactPhone(),
                saved.getCreatedAt()
        );
    }

    public List<MemberResponseDto> getAllCompanyMembers(UUID companyId, AppUserDetails principal) {

        CompanyMember caller = companyMemberRepository
                .findMemberByUserIdAndCompanyId(principal.getId(), companyId)
                .orElseThrow(() -> new ForbiddenException("Access denied"));

        if (caller.getRole() != CompanyRole.OWNER && caller.getRole() != CompanyRole.HR) {
            throw new ForbiddenException("Access denied");
        }

        return companyMemberRepository.findByCompanyId(companyId)
                .stream()
                .map(m -> new MemberResponseDto(
                        m.getId(),
                        m.getUser() != null ? m.getUser().getId() : null,
                        m.getFirstName(),
                        m.getLastName(),
                        m.getMiddleName(),
                        m.getRole(),
                        m.getMembershipStatus(),
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
                        m.getCreatedAt()
                ))
                .toList();
    }
}
