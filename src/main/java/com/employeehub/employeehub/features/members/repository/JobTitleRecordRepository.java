package com.employeehub.employeehub.features.members.repository;

import com.employeehub.employeehub.features.members.entity.JobTitleRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JobTitleRecordRepository extends JpaRepository<JobTitleRecord, UUID> {

    Optional<JobTitleRecord> findTopByCompanyMemberIdOrderByEffectiveDateDesc(UUID companyMemberId);

    Page<JobTitleRecord> findByCompanyMemberIdOrderByEffectiveDateDesc(UUID companyMemberId, Pageable pageable);
}
