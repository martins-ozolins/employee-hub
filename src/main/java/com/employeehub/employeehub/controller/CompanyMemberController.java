package com.employeehub.employeehub.controller;


import com.employeehub.employeehub.config.AppUserDetails;
import com.employeehub.employeehub.dto.ApiResponses.*;
import com.employeehub.employeehub.dto.CompanyMemberDtos.*;
import com.employeehub.employeehub.service.CompanyMemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/companies/{id}/members")
public class CompanyMemberController {

    final private CompanyMemberService companyMemberService;

    public CompanyMemberController(CompanyMemberService companyMemberService) {
        this.companyMemberService = companyMemberService;
    }


    @GetMapping
    public ResponseEntity<DataResponse<List<MemberResponseDto>>> getAllCompanyMembers(
            @PathVariable UUID id,
            @AuthenticationPrincipal AppUserDetails principal
    ) {
        List<MemberResponseDto> memberResponseDto = companyMemberService.getAllCompanyMembers(id, principal);

        return ResponseEntity.ok(new DataResponse<>(memberResponseDto));
    }


    @PostMapping
    public ResponseEntity<DataResponse<MemberResponseDto>> create(
            @PathVariable UUID id,
            @AuthenticationPrincipal AppUserDetails principal,
            @Valid @RequestBody CreateMemberDto dto
    ) {
       MemberResponseDto memberResponseDto = companyMemberService.create(id, principal, dto);

       return ResponseEntity.status(HttpStatus.CREATED).body(new DataResponse<>(memberResponseDto));
    }

}
