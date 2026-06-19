package com.employeehub.employeehub.features.roles.controller;

import com.employeehub.employeehub.features.roles.dto.CompanyRoleDtos.CompanyPermissionDto;
import com.employeehub.employeehub.features.roles.repository.CompanyPermissionRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/permissions")
public class CompanyPermissionController {

    private final CompanyPermissionRepository companyPermissionRepository;

    public CompanyPermissionController(CompanyPermissionRepository companyPermissionRepository) {
        this.companyPermissionRepository = companyPermissionRepository;
    }

    @GetMapping
    List<CompanyPermissionDto> getPermissions() {
        return companyPermissionRepository.findAll().stream()
                .map(p -> new CompanyPermissionDto(p.getId(), p.getName(), p.getDescription()))
                .toList();
    }
}
