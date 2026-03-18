package com.employeehub.employeehub.controller;


import com.employeehub.employeehub.config.AppUserDetails;
import com.employeehub.employeehub.dto.ApiResponses.*;
import com.employeehub.employeehub.dto.CompanyDtos.*;
import com.employeehub.employeehub.service.CompanyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/companies")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    // CREATE COMPANY
    @PostMapping
    public ResponseEntity<MessageAndDataResponse<CompanyResponseDto>> create(
            @AuthenticationPrincipal AppUserDetails principal,
            @RequestBody CreateCompanyDto dto
    ) {
        CompanyResponseDto company = companyService.create(principal, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new MessageAndDataResponse<>("Company created", company));
    }

    // GET COMPANIES (via CompanyMember)
    @GetMapping
    public ResponseEntity<DataResponse<List<CompanyResponseDto>>> getUserCompanies(
            @AuthenticationPrincipal AppUserDetails principal)
    {
        List<CompanyResponseDto> companies = companyService.getUserCompanies(principal);

        return ResponseEntity.status(HttpStatus.OK).body(new DataResponse<>(companies));
    }


    // UPDATE COMPANY

    // DELETE COMPANY

}