package com.employeehub.employeehub.controller;


import com.employeehub.employeehub.config.AppUserDetails;
import com.employeehub.employeehub.dto.ApiResponses.*;
import com.employeehub.employeehub.dto.CompanyMemberDtos.*;
import com.employeehub.employeehub.service.CompanyMemberService;
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

    @PostMapping
    public ResponseEntity<DataResponse<MemberResponseDto>> create(
            @PathVariable UUID id,
            @AuthenticationPrincipal AppUserDetails principal,
            @RequestBody CreateMemberDto dto
    ) {
       MemberResponseDto memberResponseDto = companyMemberService.create(id, principal, dto);

       return ResponseEntity.status(HttpStatus.CREATED).body(new DataResponse<>(memberResponseDto));
    }

}
