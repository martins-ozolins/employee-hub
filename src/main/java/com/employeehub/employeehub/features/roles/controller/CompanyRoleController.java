package com.employeehub.employeehub.features.roles.controller;

import com.employeehub.employeehub.features.roles.dto.CompanyRoleDtos.*;
import com.employeehub.employeehub.features.roles.service.CompanyRoleService;
import com.employeehub.employeehub.security.model.AuthenticatedUser;
import com.employeehub.employeehub.shared.dto.ApiResponses.*;
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
            @AuthenticationPrincipal AuthenticatedUser principal,
            @RequestParam(required = false) String search,
            Pageable pageable
    ) {
        return companyRoleService.getCompanyRoles(id, principal, search, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    RoleResponseDto createRole(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser principal,
            @RequestBody @Valid CreateRoleDto dto
    ) {
        return companyRoleService.createRole(id, principal, dto);
    }

    @PutMapping("/{roleId}")
    RoleResponseDto updateRole(
            @PathVariable UUID id,
            @PathVariable UUID roleId,
            @AuthenticationPrincipal AuthenticatedUser principal,
            @RequestBody @Valid UpdateRoleDto dto
    ) {
        return companyRoleService.updateRole(id, roleId, principal, dto);
    }

    @DeleteMapping("/{roleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteRole(
            @PathVariable UUID id,
            @PathVariable UUID roleId,
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {
        companyRoleService.deleteRole(id, roleId, principal);
    }
}
