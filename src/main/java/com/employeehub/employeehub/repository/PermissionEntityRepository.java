package com.employeehub.employeehub.repository;

import com.employeehub.employeehub.entity.PermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface PermissionEntityRepository extends JpaRepository<PermissionEntity, UUID> {
    List<PermissionEntity> findAllByNameIn(Collection<String> names);
}