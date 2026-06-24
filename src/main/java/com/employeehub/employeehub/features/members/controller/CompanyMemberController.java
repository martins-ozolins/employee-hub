package com.employeehub.employeehub.features.members.controller;

import com.employeehub.employeehub.features.members.dto.CompanyMemberDtos.*;
import com.employeehub.employeehub.features.members.service.CompanyMemberService;
import com.employeehub.employeehub.security.model.AuthenticatedUser;
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
@RequestMapping("/companies/{id}/members")
public class CompanyMemberController {

    final private CompanyMemberService companyMemberService;

    public CompanyMemberController(CompanyMemberService companyMemberService) {
        this.companyMemberService = companyMemberService;
    }

    @GetMapping
    public ResponseEntity<PagedDataResponse<?>> getAllCompanyMembers(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser principal,
            @RequestParam(required = false) String search,
            Pageable pageable
    ) {
        Page<?> page = companyMemberService.getAllCompanyMembers(id, principal, search, pageable);

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

    @GetMapping("/{memberId}")
    public ResponseEntity<DataResponse<MemberDetailDto>> getById(
            @PathVariable UUID id,
            @PathVariable UUID memberId,
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {
        MemberDetailDto dto = companyMemberService.getById(id, memberId, principal);
        return ResponseEntity.ok(new DataResponse<>(dto));
    }

    @PostMapping
    public ResponseEntity<DataResponse<MemberDetailDto>> create(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser principal,
            @Valid @RequestBody CreateMemberDto dto
    ) {
        MemberDetailDto memberDetailDto = companyMemberService.create(id, principal, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new DataResponse<>(memberDetailDto));
    }

    @PutMapping("/{memberId}")
    public ResponseEntity<DataResponse<MemberDetailDto>> update(
            @PathVariable UUID id,
            @PathVariable UUID memberId,
            @AuthenticationPrincipal AuthenticatedUser principal,
            @Valid @RequestBody UpdateMemberDto dto
    ) {
        MemberDetailDto memberDetailDto = companyMemberService.update(id, memberId, principal, dto);
        return ResponseEntity.ok(new DataResponse<>(memberDetailDto));
    }

    @DeleteMapping("/{memberId}")
    public ResponseEntity<MessageResponse> delete(
            @PathVariable UUID id,
            @PathVariable UUID memberId,
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {
        companyMemberService.delete(id, memberId, principal);
        return ResponseEntity.ok(new MessageResponse("Member deleted successfully"));
    }
}
