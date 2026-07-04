package com.employeehub.employeehub.features.members.service;

import com.employeehub.employeehub.features.auth.entity.User;
import com.employeehub.employeehub.features.auth.repository.UserRepository;
import com.employeehub.employeehub.features.email.event.EmailEvent;
import com.employeehub.employeehub.features.email.event.EmailEventPublisher;
import com.employeehub.employeehub.features.email.event.EmailEventType;
import com.employeehub.employeehub.features.members.dto.CompanyMemberDtos.*;
import com.employeehub.employeehub.features.members.entity.*;
import com.employeehub.employeehub.features.roles.entity.CompanyPermission;
import com.employeehub.employeehub.features.roles.entity.CompanyRoleEntity;
import com.employeehub.employeehub.features.roles.repository.CompanyRoleRepository;
import com.employeehub.employeehub.features.roles.service.CompanyPermissionService;
import com.employeehub.employeehub.features.members.repository.CompanyMemberRepository;
import com.employeehub.employeehub.features.members.repository.JobTitleRecordRepository;
import com.employeehub.employeehub.features.members.repository.SalaryRecordRepository;
import com.employeehub.employeehub.features.members.util.CompanyMemberUtils;
import com.employeehub.employeehub.security.model.AuthenticatedUser;
import com.employeehub.employeehub.shared.exception.ConflictException;
import com.employeehub.employeehub.shared.exception.ForbiddenException;
import com.employeehub.employeehub.shared.exception.NotFoundException;
import com.employeehub.employeehub.config.AppProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CompanyMemberService {

    private final CompanyMemberRepository companyMemberRepository;
    private final UserRepository userRepository;
    private final JobTitleRecordRepository jobTitleRecordRepository;
    private final SalaryRecordRepository salaryRecordRepository;
    private final CompanyPermissionService permissionService;
    private final CompanyRoleRepository companyRoleRepository;
    private final EmailEventPublisher emailEventPublisher;
    private final String baseUrl;

    public CompanyMemberService(
            CompanyMemberRepository companyMemberRepository,
            UserRepository userRepository,
            JobTitleRecordRepository jobTitleRecordRepository,
            SalaryRecordRepository salaryRecordRepository,
            CompanyPermissionService permissionService,
            CompanyRoleRepository companyRoleRepository,
            EmailEventPublisher emailEventPublisher,
            AppProperties appProperties
    ) {
        this.companyMemberRepository = companyMemberRepository;
        this.userRepository = userRepository;
        this.jobTitleRecordRepository = jobTitleRecordRepository;
        this.salaryRecordRepository = salaryRecordRepository;
        this.permissionService = permissionService;
        this.companyRoleRepository = companyRoleRepository;
        this.emailEventPublisher = emailEventPublisher;
        this.baseUrl = appProperties.baseUrl();
    }

    @Transactional
    public MemberDetailDto create(UUID companyId, AuthenticatedUser principal, CreateMemberDto dto) {

        CompanyMember caller = permissionService.getCallerOrThrow(principal, companyId);
        permissionService.checkPermission(caller, CompanyPermission.MANAGE_MEMBERS);

        if (companyMemberRepository.existsByPersonalEmailAndCompany(dto.personalEmail(), caller.getCompany())) {
            throw new ConflictException("A member with this email already exists in the company");
        }

        // Auto-link if a user with this email already exists
        User linkedUser = userRepository.findByEmail(dto.personalEmail()).orElse(null);

        CompanyRoleEntity role = companyRoleRepository.findByCompanyIdAndId(caller.getCompany().getId(), dto.roleId())
                .orElseThrow(() -> new NotFoundException("Role not found"));

        CompanyMember newMember = CompanyMember.builder()
                .user(linkedUser)
                .company(caller.getCompany())
                .firstName(dto.firstName())
                .lastName(dto.lastName())
                .middleName(dto.middleName())
                .companyRole(role)
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

        if (dto.selfServiceEnabled()) {
            saved.setInvitedAt(Instant.now());
            companyMemberRepository.save(saved);
            Map<String, String> data = new HashMap<>();
            data.put("companyName", saved.getCompany().getName());
            data.put("memberName", saved.getFirstName());
            data.put("baseUrl", baseUrl);
            emailEventPublisher.publish(new EmailEvent(EmailEventType.MEMBER_INVITED, saved.getPersonalEmail(), data));
        }

        if (dto.jobTitle() != null && !dto.jobTitle().isBlank()) {
            JobTitleRecord initialRecord = JobTitleRecord.builder()
                    .companyMember(saved)
                    .jobTitle(dto.jobTitle())
                    .changeType(TitleChangeType.INITIAL)
                    .effectiveDate(dto.joinDate())
                    .build();
            jobTitleRecordRepository.save(initialRecord);
        }

        if (dto.initialSalaryAmount() != null && dto.initialSalaryCurrency() != null) {
            SalaryRecord initialSalary = SalaryRecord.builder()
                    .companyMember(saved)
                    .amount(dto.initialSalaryAmount())
                    .currency(dto.initialSalaryCurrency())
                    .effectiveDate(dto.joinDate())
                    .build();
            salaryRecordRepository.save(initialSalary);
            saved.setCurrentSalaryAmount(dto.initialSalaryAmount());
            saved.setCurrentSalaryCurrency(dto.initialSalaryCurrency());
            companyMemberRepository.save(saved);
        }

        return CompanyMemberUtils.toDetailDto(saved);
    }

    public Page<?> getAllCompanyMembers(UUID companyId, AuthenticatedUser principal, String search, Pageable pageable) {

        CompanyMember caller = permissionService.getCallerOrThrow(principal, companyId);
        permissionService.checkPermission(caller, CompanyPermission.VIEW_MEMBERS);

        String normalizedSearch = (search == null || search.isBlank()) ? "" : search.trim();

        Page<CompanyMember> members = companyMemberRepository.searchByCompanyId(companyId, normalizedSearch, pageable);

        if (permissionService.hasPermission(caller, CompanyPermission.VIEW_MEMBER_DETAILS)) {
            return members.map(CompanyMemberUtils::toSummaryDto);
        }

        return members.map(CompanyMemberUtils::toDirectoryDto);
    }

    public MemberDetailDto getById(UUID companyId, UUID memberId, AuthenticatedUser principal) {

        CompanyMember caller = permissionService.getCallerOrThrow(principal, companyId);
        permissionService.checkPermission(caller, CompanyPermission.VIEW_MEMBER_DETAILS);

        CompanyMember member = companyMemberRepository.findByCompanyIdAndId(companyId, memberId)
                .orElseThrow(() -> new NotFoundException("Member not found"));

        return CompanyMemberUtils.toDetailDto(member);
    }

    @Transactional
    public MemberDetailDto update(UUID companyId, UUID memberId, AuthenticatedUser principal, UpdateMemberDto dto) {

        CompanyMember caller = permissionService.getCallerOrThrow(principal, companyId);
        permissionService.checkPermission(caller, CompanyPermission.MANAGE_MEMBERS);

        CompanyMember member = companyMemberRepository.findByCompanyIdAndId(companyId, memberId)
                .orElseThrow(() -> new NotFoundException("Member not found"));

        if (!member.getPersonalEmail().equals(dto.personalEmail()) &&
                companyMemberRepository.existsByPersonalEmailAndCompany(dto.personalEmail(), member.getCompany())) {
            throw new ConflictException("A member with this email already exists in the company");
        }

        boolean emailChanged = !member.getPersonalEmail().equals(dto.personalEmail());
        if (emailChanged) {
            member.setInvitedAt(null);
        }

        member.setFirstName(dto.firstName());
        member.setLastName(dto.lastName());
        member.setMiddleName(dto.middleName());
        member.setPersonalEmail(dto.personalEmail());
        CompanyRoleEntity updatedRole = companyRoleRepository.findByCompanyIdAndId(member.getCompany().getId(), dto.roleId())
                .orElseThrow(() -> new NotFoundException("Role not found"));
        member.setCompanyRole(updatedRole);
        member.setEmploymentStatus(dto.employmentStatus());
        member.setSelfServiceEnabled(dto.selfServiceEnabled());
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

        if (dto.selfServiceEnabled() && saved.getInvitedAt() == null) {
            saved.setInvitedAt(Instant.now());
            companyMemberRepository.save(saved);
            Map<String, String> data = new HashMap<>();
            data.put("companyName", saved.getCompany().getName());
            data.put("memberName", saved.getFirstName());
            data.put("baseUrl", baseUrl);
            emailEventPublisher.publish(new EmailEvent(EmailEventType.MEMBER_INVITED, saved.getPersonalEmail(), data));
        }

        return CompanyMemberUtils.toDetailDto(saved);
    }

    public void delete(UUID companyId, UUID memberId, AuthenticatedUser principal) {

        CompanyMember caller = permissionService.getCallerOrThrow(principal, companyId);
        permissionService.checkPermission(caller, CompanyPermission.MANAGE_MEMBERS);

        CompanyMember target = companyMemberRepository.findByCompanyIdAndId(companyId, memberId)
                .orElseThrow(() -> new NotFoundException("Member not found"));

        if (target.getId().equals(caller.getId())) {
            throw new ForbiddenException("You cannot delete yourself");
        }

        CompanyRoleEntity targetRole = target.getCompanyRole();
        if (targetRole.isSystem() && targetRole.getName().equals("WORKSPACE_OWNER") &&
                companyMemberRepository.countByCompanyIdAndCompanyRole(companyId, targetRole) <= 1) {
            throw new ForbiddenException("Cannot delete the last owner. Please assign another owner first");
        }

        companyMemberRepository.delete(target);
    }


    public MemberSelfResponseDto getSelf(UUID companyId, AuthenticatedUser principal) {

        CompanyMember caller = permissionService.getCallerOrThrow(principal, companyId);
        permissionService.checkSelfServiceAccess(caller);

        Set<String> permissions = permissionService.getPermissions(caller).stream()
                .map(CompanyPermission::name)
                .collect(Collectors.toSet());
        return CompanyMemberUtils.toSelfDto(caller, permissions);
    }

    @Transactional
    public MemberSelfResponseDto updateSelf(UUID companyId, AuthenticatedUser principal, MemberSelfUpdateDto dto) {

        CompanyMember caller = permissionService.getCallerOrThrow(principal, companyId);
        permissionService.checkSelfServiceAccess(caller);

        caller.setPhoneNumber(dto.phoneNumber());
        caller.setAddress(dto.address());
        caller.setBankAccount(dto.bankAccount());
        caller.setEmergencyContactName(dto.emergencyContactName());
        caller.setEmergencyContactPhone(dto.emergencyContactPhone());

        CompanyMember saved = companyMemberRepository.saveAndFlush(caller);

        Set<String> savedPermissions = permissionService.getPermissions(saved).stream()
                .map(CompanyPermission::name)
                .collect(Collectors.toSet());
        return CompanyMemberUtils.toSelfDto(saved, savedPermissions);
    }

}
