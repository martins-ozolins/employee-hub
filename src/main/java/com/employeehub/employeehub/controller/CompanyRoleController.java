package com.employeehub.employeehub.controller;


import com.employeehub.employeehub.config.AppUserDetails;
import com.employeehub.employeehub.dto.ApiResponses.*;
import com.employeehub.employeehub.dto.CompanyRoleDtos.*;
import com.employeehub.employeehub.service.CompanyRoleService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/companies/{id}/roles")
public class CompanyRoleController {

    private final CompanyRoleService companyRoleService;

    public CompanyRoleController(CompanyRoleService companyRoleService) {
        this.companyRoleService = companyRoleService;
    }

    @GetMapping
    PagedDataResponse<RoleResponseDto> getCompanyRoles(
            @PathVariable UUID id,
            @AuthenticationPrincipal AppUserDetails principal,
            @RequestParam(required = false) String search,
            Pageable pageable
    ) {
        return companyRoleService.getCompanyRoles(id, principal, search, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    RoleResponseDto createRole(
            @PathVariable UUID id,
            @AuthenticationPrincipal AppUserDetails principal,
            @RequestBody @Valid CreateRoleDto dto
    ) {
        return companyRoleService.createRole(id, principal, dto);
    }

    @PutMapping("/{roleId}")
    RoleResponseDto updateRole(
            @PathVariable UUID id,
            @PathVariable UUID roleId,
            @AuthenticationPrincipal AppUserDetails principal,
            @RequestBody @Valid UpdateRoleDto dto
    ) {
        return companyRoleService.updateRole(id, roleId, principal, dto);
    }

    @DeleteMapping("/{roleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteRole(
            @PathVariable UUID id,
            @PathVariable UUID roleId,
            @AuthenticationPrincipal AppUserDetails principal
    ) {
        companyRoleService.deleteRole(id, roleId, principal);
    }
}
