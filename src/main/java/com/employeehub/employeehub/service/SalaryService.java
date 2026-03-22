package com.employeehub.employeehub.service;

import com.employeehub.employeehub.config.AppUserDetails;
import com.employeehub.employeehub.dto.SalaryDtos.*;
import com.employeehub.employeehub.entity.CompanyMember;
import com.employeehub.employeehub.entity.CompanyRole;
import com.employeehub.employeehub.entity.SalaryRecord;
import com.employeehub.employeehub.exception.ForbiddenException;
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

    public SalaryService(
            CompanyMemberRepository companyMemberRepository,
            SalaryRecordRepository salaryRecordRepository
    ) {
        this.companyMemberRepository = companyMemberRepository;
        this.salaryRecordRepository = salaryRecordRepository;
    }

    @Transactional
    public SalaryRecordDto add(UUID companyId, UUID memberId, AppUserDetails principal, AddSalaryDto dto) {

        CompanyMember caller = companyMemberRepository
                .findByUserIdAndCompanyId(principal.getId(), companyId)
                .orElseThrow(() -> new ForbiddenException("Access denied"));

        if (caller.getRole() != CompanyRole.OWNER && caller.getRole() != CompanyRole.HR) {
            throw new ForbiddenException("Access denied");
        }

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

        CompanyMember caller = companyMemberRepository
                .findByUserIdAndCompanyId(principal.getId(), companyId)
                .orElseThrow(() -> new ForbiddenException("Access denied"));

        if (caller.getRole() != CompanyRole.OWNER && caller.getRole() != CompanyRole.HR) {
            throw new ForbiddenException("Access denied");
        }

        companyMemberRepository.findByCompanyIdAndId(companyId, memberId)
                .orElseThrow(() -> new NotFoundException("Member not found"));

        return salaryRecordRepository
                .findByCompanyMemberIdOrderByEffectiveDateDesc(memberId, pageable)
                .map(SalaryUtils::toDto);
    }
}