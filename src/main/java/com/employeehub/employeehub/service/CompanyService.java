package com.employeehub.employeehub.service;

import com.employeehub.employeehub.config.AppUserDetails;
import com.employeehub.employeehub.dto.CompanyDtos.*;
import com.employeehub.employeehub.entity.*;
import com.employeehub.employeehub.exception.ForbiddenException;
import com.employeehub.employeehub.exception.NotFoundException;
import com.employeehub.employeehub.repository.CompanyMemberRepository;
import com.employeehub.employeehub.repository.CompanyRepository;
import com.employeehub.employeehub.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMemberRepository companyMemberRepository;
    private final UserRepository userRepository;

    public CompanyService(
            CompanyRepository companyRepository,
            CompanyMemberRepository companyMemberRepository,
            UserRepository userRepository
    ) {
        this.companyRepository = companyRepository;
        this.companyMemberRepository = companyMemberRepository;
        this.userRepository = userRepository;
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

        CompanyMember owner = CompanyMember.builder()
                .user(user)
                .company(company)
                .role(CompanyRole.OWNER)
                .membershipStatus(MembershipStatus.ACTIVE)
                .employmentStatus(EmploymentStatus.ACTIVE)
                .selfServiceEnabled(true)
                .workEmail(user.getEmail())
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

    public CompanyResponseDto update(AppUserDetails principal, UpdateCompanyDto dto, UUID companyId) {

        // check if user is part of the company and if has right roles
        CompanyMember member = companyMemberRepository
                .findMemberByUserIdAndCompanyId(principal.getId(), companyId)
                .orElseThrow(() -> new ForbiddenException("Access denied"));

        if (member.getRole() != CompanyRole.OWNER && member.getRole() != CompanyRole.HR) {
            throw new ForbiddenException("Access denied");
        }

        Company company = member.getCompany();
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

        // check if user is part of the company and if has right roles
        CompanyMember member = companyMemberRepository
                .findMemberByUserIdAndCompanyId(principal.getId(), companyId)
                .orElseThrow(() -> new ForbiddenException("Access denied"));

        if (member.getRole() != CompanyRole.OWNER && member.getRole() != CompanyRole.HR) {
            throw new ForbiddenException("Access denied");
        }

        Company company = member.getCompany();

        companyRepository.delete(company);

    }
}

