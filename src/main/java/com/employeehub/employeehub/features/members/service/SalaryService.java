package com.employeehub.employeehub.features.members.service;

import com.employeehub.employeehub.features.members.dto.SalaryDtos.*;
import com.employeehub.employeehub.features.members.entity.CompanyMember;
import com.employeehub.employeehub.features.members.entity.SalaryRecord;
import com.employeehub.employeehub.features.members.repository.CompanyMemberRepository;
import com.employeehub.employeehub.features.members.repository.SalaryRecordRepository;
import com.employeehub.employeehub.features.members.util.SalaryUtils;
import com.employeehub.employeehub.features.roles.entity.CompanyPermission;
import com.employeehub.employeehub.features.roles.service.CompanyPermissionService;
import com.employeehub.employeehub.security.model.AppUserDetails;
import com.employeehub.employeehub.shared.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class SalaryService {

    private final CompanyMemberRepository companyMemberRepository;
    private final SalaryRecordRepository salaryRecordRepository;
    private final CompanyPermissionService permissionService;

    public SalaryService(
            CompanyMemberRepository companyMemberRepository,
            SalaryRecordRepository salaryRecordRepository,
            CompanyPermissionService permissionService
    ) {
        this.companyMemberRepository = companyMemberRepository;
        this.salaryRecordRepository = salaryRecordRepository;
        this.permissionService = permissionService;
    }

    @Transactional
    public SalaryRecordDto add(UUID companyId, UUID memberId, AppUserDetails principal, AddSalaryDto dto) {

        CompanyMember caller = permissionService.getCallerOrThrow(principal, companyId);
        permissionService.checkPermission(caller, CompanyPermission.MANAGE_SALARY);

        CompanyMember member = companyMemberRepository.findByCompanyIdAndId(companyId, memberId)
                .orElseThrow(() -> new NotFoundException("Member not found"));

        SalaryRecord record = SalaryRecord.builder()
                .companyMember(member)
                .amount(dto.amount())
                .currency(dto.currency())
                .effectiveDate(dto.effectiveDate())
                .notes(dto.notes())
                .build();

        SalaryRecord saved = salaryRecordRepository.saveAndFlush(record);

        member.setCurrentSalaryAmount(dto.amount());
        member.setCurrentSalaryCurrency(dto.currency());
        companyMemberRepository.save(member);

        return SalaryUtils.toDto(saved);
    }

    public Page<SalaryRecordDto> getHistory(UUID companyId, UUID memberId, AppUserDetails principal, Pageable pageable) {

        CompanyMember caller = permissionService.getCallerOrThrow(principal, companyId);
        permissionService.checkPermission(caller, CompanyPermission.VIEW_MEMBER_DETAILS);

        companyMemberRepository.findByCompanyIdAndId(companyId, memberId)
                .orElseThrow(() -> new NotFoundException("Member not found"));

        return salaryRecordRepository
                .findByCompanyMemberIdOrderByEffectiveDateDesc(memberId, pageable)
                .map(SalaryUtils::toDto);
    }
}
