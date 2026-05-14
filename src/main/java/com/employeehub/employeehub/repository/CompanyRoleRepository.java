package com.employeehub.employeehub.repository;

import com.employeehub.employeehub.entity.CompanyRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompanyRoleRepository extends JpaRepository<CompanyRoleEntity, UUID> {
    List<CompanyRoleEntity> findByCompanyId(UUID companyId);

    Optional<CompanyRoleEntity> findByCompanyIdAndId(UUID companyId, UUID id);

    Optional<CompanyRoleEntity> findByCompanyIdAndName(UUID companyId, String name);

    boolean existsByCompanyIdAndName(UUID companyId, String name);

    List<CompanyRoleEntity> findByCompanyIdAndIsSystemTrue(UUID companyId);
}