package com.employeehub.employeehub.features.members.controller;

import com.employeehub.employeehub.features.members.dto.SalaryDtos.*;
import com.employeehub.employeehub.features.members.service.SalaryService;
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
@RequestMapping("/companies/{id}/members/{memberId}/salary")
public class SalaryController {

    private final SalaryService salaryService;

    public SalaryController(SalaryService salaryService) {
        this.salaryService = salaryService;
    }

    @PostMapping
    public ResponseEntity<DataResponse<SalaryRecordDto>> add(
            @PathVariable UUID id,
            @PathVariable UUID memberId,
            @AuthenticationPrincipal AppUserDetails principal,
            @Valid @RequestBody AddSalaryDto dto
    ) {
        SalaryRecordDto record = salaryService.add(id, memberId, principal, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new DataResponse<>(record));
    }

    @GetMapping
    public ResponseEntity<PagedDataResponse<SalaryRecordDto>> getHistory(
            @PathVariable UUID id,
            @PathVariable UUID memberId,
            @AuthenticationPrincipal AppUserDetails principal,
            Pageable pageable
    ) {
        Page<SalaryRecordDto> page = salaryService.getHistory(id, memberId, principal, pageable);

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
