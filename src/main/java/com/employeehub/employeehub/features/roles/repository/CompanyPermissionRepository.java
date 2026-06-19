package com.employeehub.employeehub.features.roles.repository;

import com.employeehub.employeehub.features.roles.entity.CompanyPermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface CompanyPermissionRepository extends JpaRepository<CompanyPermissionEntity, UUID> {
    List<CompanyPermissionEntity> findAllByNameIn(Collection<String> names);
}
