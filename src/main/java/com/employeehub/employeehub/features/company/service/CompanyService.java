package com.employeehub.employeehub.features.company.service;

import com.employeehub.employeehub.features.auth.entity.User;
import com.employeehub.employeehub.features.auth.repository.UserRepository;
import com.employeehub.employeehub.features.company.dto.CompanyDtos.*;
import com.employeehub.employeehub.features.company.entity.Company;
import com.employeehub.employeehub.features.company.repository.CompanyRepository;
import com.employeehub.employeehub.features.members.entity.CompanyMember;
import com.employeehub.employeehub.features.members.entity.EmploymentStatus;
import com.employeehub.employeehub.features.members.repository.CompanyMemberRepository;
import com.employeehub.employeehub.features.roles.entity.CompanyPermission;
import com.employeehub.employeehub.features.roles.entity.CompanyRoleEntity;
import com.employeehub.employeehub.features.roles.repository.CompanyPermissionRepository;
import com.employeehub.employeehub.features.roles.repository.CompanyRoleRepository;
import com.employeehub.employeehub.features.roles.service.CompanyPermissionService;
import com.employeehub.employeehub.security.model.AppUserDetails;
import com.employeehub.employeehub.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMemberRepository companyMemberRepository;
    private final UserRepository userRepository;
    private final CompanyPermissionService permissionService;
    private final CompanyRoleRepository companyRoleRepository;
    private final CompanyPermissionRepository companyPermissionRepository;

    public CompanyService(
            CompanyRepository companyRepository,
            CompanyMemberRepository companyMemberRepository,
            UserRepository userRepository,
            CompanyPermissionService permissionService,
            CompanyRoleRepository companyRoleRepository,
            CompanyPermissionRepository companyPermissionRepository
    ) {
        this.companyRepository = companyRepository;
        this.companyMemberRepository = companyMemberRepository;
        this.userRepository = userRepository;
        this.permissionService = permissionService;
        this.companyRoleRepository = companyRoleRepository;
        this.companyPermissionRepository = companyPermissionRepository;
    }

    @Transactional
    public CompanyResponseDto create(AppUserDetails principal, CreateCompanyDto dto) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        Company company = Company.builder()
                .name(dto.name())
                .industry(dto.industry())
                .location(dto.location())
                .description(dto.description())
                .build();

        company = companyRepository.saveAndFlush(company);

        CompanyRoleEntity ownerRole = CompanyRoleEntity.builder()
                .company(company)
                .name("WORKSPACE_OWNER")
                .isSystem(true)
                .permissions(new java.util.HashSet<>(companyPermissionRepository.findAll()))
                .build();
        ownerRole = companyRoleRepository.save(ownerRole);

        CompanyMember owner = CompanyMember.builder()
                .user(user)
                .company(company)
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .middleName(user.getMiddleName())
                .companyRole(ownerRole)
                .employmentStatus(EmploymentStatus.ACTIVE)
                .selfServiceEnabled(true)
                .personalEmail(user.getEmail())
                .build();

        companyMemberRepository.save(owner);

        return new CompanyResponseDto(
                company.getId(),
                company.getName(),
                company.getIndustry(),
                company.getLocation(),
                company.getDescription(),
                company.getCreatedAt()
        );
    }

    public List<CompanyResponseDto> getUserCompanies(AppUserDetails principal) {
        return companyMemberRepository.findCompaniesByUserId(principal.getId())
                .stream()
                .map(company -> new CompanyResponseDto(
                        company.getId(),
                        company.getName(),
                        company.getIndustry(),
                        company.getLocation(),
                        company.getDescription(),
                        company.getCreatedAt()
                ))
                .toList();
    }

    public Object getById(AppUserDetails principal, UUID companyId) {

        CompanyMember caller = permissionService.getCallerOrThrow(principal, companyId);

        Company company = caller.getCompany();

        if (permissionService.hasPermission(caller, CompanyPermission.MANAGE_COMPANY)) {
            return new CompanyResponseDto(
                    company.getId(),
                    company.getName(),
                    company.getIndustry(),
                    company.getLocation(),
                    company.getDescription(),
                    company.getCreatedAt()
            );
        }

        return new CompanyBasicDto(
                company.getId(),
                company.getName(),
                company.getIndustry(),
                company.getCreatedAt()
        );
    }

    @Transactional
    public CompanyResponseDto update(AppUserDetails principal, UpdateCompanyDto dto, UUID companyId) {

        CompanyMember caller = permissionService.getCallerOrThrow(principal, companyId);
        permissionService.checkPermission(caller, CompanyPermission.MANAGE_COMPANY);

        Company company = caller.getCompany();
        company.setName(dto.name());
        company.setIndustry(dto.industry());
        company.setLocation(dto.location());
        company.setDescription(dto.description());

        company = companyRepository.saveAndFlush(company);

        return new CompanyResponseDto(
                company.getId(),
                company.getName(),
                company.getIndustry(),
                company.getLocation(),
                company.getDescription(),
                company.getCreatedAt()
        );
    }

    @Transactional
    public void delete(AppUserDetails principal, UUID companyId) {

        CompanyMember caller = permissionService.getCallerOrThrow(principal, companyId);
        permissionService.checkPermission(caller, CompanyPermission.MANAGE_COMPANY);

        companyRepository.delete(caller.getCompany());
    }
}
