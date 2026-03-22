package com.employeehub.employeehub.repository;

import com.employeehub.employeehub.entity.SalaryRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SalaryRecordRepository extends JpaRepository<SalaryRecord, UUID> {

    Optional<SalaryRecord> findTopByCompanyMemberIdOrderByEffectiveDateDesc(UUID companyMemberId);

    Page<SalaryRecord> findByCompanyMemberIdOrderByEffectiveDateDesc(UUID companyMemberId, Pageable pageable);
}