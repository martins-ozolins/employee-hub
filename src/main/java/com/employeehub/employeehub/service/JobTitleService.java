package com.employeehub.employeehub.service;

import com.employeehub.employeehub.config.AppUserDetails;
import com.employeehub.employeehub.dto.JobTitleDtos.*;
import com.employeehub.employeehub.entity.CompanyMember;
import com.employeehub.employeehub.entity.JobTitleRecord;
import com.employeehub.employeehub.entity.Permission;
import com.employeehub.employeehub.exception.NotFoundException;
import com.employeehub.employeehub.repository.CompanyMemberRepository;
import com.employeehub.employeehub.repository.JobTitleRecordRepository;
import com.employeehub.employeehub.util.JobTitleUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class JobTitleService {

    private final CompanyMemberRepository companyMemberRepository;
    private final JobTitleRecordRepository jobTitleRecordRepository;
    private final PermissionService permissionService;

    public JobTitleService(
            CompanyMemberRepository companyMemberRepository,
            JobTitleRecordRepository jobTitleRecordRepository,
            PermissionService permissionService
    ) {
        this.companyMemberRepository = companyMemberRepository;
        this.jobTitleRecordRepository = jobTitleRecordRepository;
        this.permissionService = permissionService;
    }

    @Transactional
    public JobTitleRecordDto add(UUID companyId, UUID memberId, AppUserDetails principal, AddJobTitleDto dto) {

        CompanyMember caller = permissionService.getCallerOrThrow(principal, companyId);
        permissionService.checkPermission(caller, Permission.MANAGE_JOB_TITLES);

        CompanyMember member = companyMemberRepository.findByCompanyIdAndId(companyId, memberId)
                .orElseThrow(() -> new NotFoundException("Member not found"));

        JobTitleRecord record = JobTitleRecord.builder()
                .companyMember(member)
                .jobTitle(dto.jobTitle())
                .changeType(dto.changeType())
                .effectiveDate(dto.effectiveDate())
                .notes(dto.notes())
                .build();

        JobTitleRecord saved = jobTitleRecordRepository.saveAndFlush(record);

        member.setJobTitle(dto.jobTitle());
        companyMemberRepository.save(member);

        return JobTitleUtils.toDto(saved);
    }

    public Page<JobTitleRecordDto> getHistory(UUID companyId, UUID memberId, AppUserDetails principal, Pageable pageable) {

        CompanyMember caller = permissionService.getCallerOrThrow(principal, companyId);
        permissionService.checkPermission(caller, Permission.VIEW_MEMBER_DETAILS);

        companyMemberRepository.findByCompanyIdAndId(companyId, memberId)
                .orElseThrow(() -> new NotFoundException("Member not found"));

        return jobTitleRecordRepository
                .findByCompanyMemberIdOrderByEffectiveDateDesc(memberId, pageable)
                .map(JobTitleUtils::toDto);
    }
}