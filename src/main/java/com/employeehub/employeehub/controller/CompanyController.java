package com.employeehub.employeehub.controller;


import com.employeehub.employeehub.config.AppUserDetails;
import com.employeehub.employeehub.dto.ApiResponse;
import com.employeehub.employeehub.dto.CompanyDtos.*;
import com.employeehub.employeehub.service.CompanyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/companies")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    // CREATE COMPANY
    @PostMapping
    public ResponseEntity<ApiResponse> create(
            @AuthenticationPrincipal AppUserDetails principal,
            @RequestBody CreateCompanyDto dto
    ) {
        CompanyResponseDto company = companyService.create(principal, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>("Company created", company));
    }


    // GET COMPANIES (via CompanyMember)


    // UPDATE COMPANY


    // DELETE COMPANY

}
