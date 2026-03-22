package com.employeehub.employeehub.service;

import com.employeehub.employeehub.config.AppUserDetails;
import com.employeehub.employeehub.dto.CompanyMemberDtos.*;
import com.employeehub.employeehub.entity.*;
import com.employeehub.employeehub.exception.ConflictException;
import com.employeehub.employeehub.exception.ForbiddenException;
import com.employeehub.employeehub.exception.NotFoundException;
import com.employeehub.employeehub.repository.CompanyMemberRepository;
import com.employeehub.employeehub.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                .findByUserIdAndCompanyId(principal.getId(), companyId)
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

    public Page<MemberResponseDto> getAllCompanyMembers(UUID companyId, AppUserDetails principal, String search, Pageable pageable) {

        CompanyMember caller = companyMemberRepository
                .findByUserIdAndCompanyId(principal.getId(), companyId)
                .orElseThrow(() -> new ForbiddenException("Access denied"));

        if (caller.getRole() != CompanyRole.OWNER && caller.getRole() != CompanyRole.HR) {
            throw new ForbiddenException("Access denied");
        }

        String normalizedSearch = (search == null || search.isBlank()) ? "" : search.trim();

        return companyMemberRepository.searchByCompanyId(companyId, normalizedSearch, pageable)
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
                ));
    }

    @Transactional
    public MemberResponseDto update(UUID companyId, UUID memberId, AppUserDetails principal, UpdateMemberDto dto) {

        CompanyMember caller = companyMemberRepository
                .findByUserIdAndCompanyId(principal.getId(), companyId)
                .orElseThrow(() -> new ForbiddenException("Access denied"));

        if (caller.getRole() != CompanyRole.OWNER && caller.getRole() != CompanyRole.HR) {
            throw new ForbiddenException("Access denied");
        }

        CompanyMember member = companyMemberRepository.findByCompanyIdAndId(companyId, memberId)
                .orElseThrow(() -> new NotFoundException("Member not found"));

        if (!member.getPersonalEmail().equals(dto.personalEmail()) &&
                companyMemberRepository.existsByPersonalEmailAndCompany(dto.personalEmail(), member.getCompany())) {
            throw new ConflictException("A member with this email already exists in the company");
        }

        member.setFirstName(dto.firstName());
        member.setLastName(dto.lastName());
        member.setMiddleName(dto.middleName());
        member.setPersonalEmail(dto.personalEmail());
        member.setRole(dto.role());
        member.setMembershipStatus(dto.membershipStatus());
        member.setEmploymentStatus(dto.employmentStatus());
        member.setSelfServiceEnabled(dto.selfServiceEnabled());
        member.setJobTitle(dto.jobTitle());
        member.setDepartment(dto.department());
        member.setJoinDate(dto.joinDate());
        member.setWorkEmail(dto.workEmail());
        member.setPhoneNumber(dto.phoneNumber());
        member.setDateOfBirth(dto.dateOfBirth());
        member.setAddress(dto.address());
        member.setPersonalCode(dto.personalCode());
        member.setBankAccount(dto.bankAccount());
        member.setEmergencyContactName(dto.emergencyContactName());
        member.setEmergencyContactPhone(dto.emergencyContactPhone());

        CompanyMember saved = companyMemberRepository.saveAndFlush(member);

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

    public void delete(UUID companyId, UUID memberId, AppUserDetails principal) {

        CompanyMember caller = companyMemberRepository
                .findByUserIdAndCompanyId(principal.getId(), companyId)
                .orElseThrow(() -> new ForbiddenException("Access denied"));

        if (caller.getRole() != CompanyRole.OWNER && caller.getRole() != CompanyRole.HR) {
            throw new ForbiddenException("Access denied");
        }

        CompanyMember target = companyMemberRepository.findByCompanyIdAndId(companyId, memberId)
                .orElseThrow(() -> new NotFoundException("Member not found"));

        if (target.getId().equals(caller.getId())) {
            throw new ForbiddenException("You cannot delete yourself");
        }

        if (target.getRole() == CompanyRole.OWNER &&
                companyMemberRepository.countByCompanyIdAndRole(companyId, CompanyRole.OWNER) <= 1) {
            throw new ForbiddenException("Cannot delete the last owner. Please assign another owner first");
        }

        companyMemberRepository.delete(target);

    }
}
