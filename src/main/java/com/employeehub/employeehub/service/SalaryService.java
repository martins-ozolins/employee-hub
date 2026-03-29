package com.employeehub.employeehub.service;

import com.employeehub.employeehub.config.AppUserDetails;
import com.employeehub.employeehub.dto.SalaryDtos.*;
import com.employeehub.employeehub.entity.CompanyMember;
import com.employeehub.employeehub.entity.Permission;
import com.employeehub.employeehub.entity.SalaryRecord;
import com.employeehub.employeehub.exception.NotFoundException;
import com.employeehub.employeehub.repository.CompanyMemberRepository;
import com.employeehub.employeehub.repository.SalaryRecordRepository;
import com.employeehub.employeehub.util.SalaryUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class SalaryService {

    private final CompanyMemberRepository companyMemberRepository;
    private final SalaryRecordRepository salaryRecordRepository;
    private final PermissionService permissionService;

    public SalaryService(
            CompanyMemberRepository companyMemberRepository,
            SalaryRecordRepository salaryRecordRepository,
            PermissionService permissionService
    ) {
        this.companyMemberRepository = companyMemberRepository;
        this.salaryRecordRepository = salaryRecordRepository;
        this.permissionService = permissionService;
    }

    @Transactional
    public SalaryRecordDto add(UUID companyId, UUID memberId, AppUserDetails principal, AddSalaryDto dto) {

        CompanyMember caller = permissionService.getCallerOrThrow(principal, companyId);
        permissionService.checkPermission(caller, Permission.MANAGE_SALARY);

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
        permissionService.checkPermission(caller, Permission.VIEW_MEMBER_DETAILS);

        companyMemberRepository.findByCompanyIdAndId(companyId, memberId)
                .orElseThrow(() -> new NotFoundException("Member not found"));

        return salaryRecordRepository
                .findByCompanyMemberIdOrderByEffectiveDateDesc(memberId, pageable)
                .map(SalaryUtils::toDto);
    }
}