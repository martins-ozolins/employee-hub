package com.employeehub.employeehub.features.roles.repository;

import com.employeehub.employeehub.features.roles.entity.CompanyRoleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompanyRoleRepository extends JpaRepository<CompanyRoleEntity, UUID> {

    @Query("SELECT r FROM CompanyRoleEntity r WHERE r.company.id = :companyId AND (:search IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<CompanyRoleEntity> findByCompanyId(UUID companyId, String search, Pageable pageable);

    Optional<CompanyRoleEntity> findByCompanyIdAndId(UUID companyId, UUID id);

    Optional<CompanyRoleEntity> findByCompanyIdAndName(UUID companyId, String name);

    boolean existsByCompanyIdAndName(UUID companyId, String name);

    List<CompanyRoleEntity> findByCompanyIdAndIsSystemTrue(UUID companyId);
}
