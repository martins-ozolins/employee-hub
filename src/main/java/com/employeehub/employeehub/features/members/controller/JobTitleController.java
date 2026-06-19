package com.employeehub.employeehub.features.members.controller;

import com.employeehub.employeehub.features.members.dto.JobTitleDtos.*;
import com.employeehub.employeehub.features.members.service.JobTitleService;
import com.employeehub.employeehub.security.model.AppUserDetails;
import com.employeehub.employeehub.shared.dto.ApiResponses.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/companies/{id}/members/{memberId}/job-title")
public class JobTitleController {

    private final JobTitleService jobTitleService;

    public JobTitleController(JobTitleService jobTitleService) {
        this.jobTitleService = jobTitleService;
    }

    @PostMapping
    public ResponseEntity<DataResponse<JobTitleRecordDto>> add(
            @PathVariable UUID id,
            @PathVariable UUID memberId,
            @AuthenticationPrincipal AppUserDetails principal,
            @Valid @RequestBody AddJobTitleDto dto
    ) {
        JobTitleRecordDto record = jobTitleService.add(id, memberId, principal, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new DataResponse<>(record));
    }

    @GetMapping
    public ResponseEntity<PagedDataResponse<JobTitleRecordDto>> getHistory(
            @PathVariable UUID id,
            @PathVariable UUID memberId,
            @AuthenticationPrincipal AppUserDetails principal,
            Pageable pageable
    ) {
        Page<JobTitleRecordDto> page = jobTitleService.getHistory(id, memberId, principal, pageable);

        return ResponseEntity.ok(new PagedDataResponse<>(
                page.getContent(),
                new PageMeta(
                        page.getNumber() + 1,
                        page.getSize(),
                        page.getTotalElements(),
                        page.getTotalPages(),
                        page.hasNext(),
                        page.hasPrevious()
                )
        ));
    }
}
