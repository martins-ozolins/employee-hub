package com.employeehub.employeehub.features.documents.repository;

import com.employeehub.employeehub.features.documents.entity.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {

    Page<Document> findByCompanyMemberIdOrderByUploadedAtDesc(UUID companyMemberId, Pageable pageable);

    Optional<Document> findByIdAndCompanyMemberId(UUID id, UUID companyMemberId);

    @Query("""
            SELECT d FROM Document d
            JOIN FETCH d.companyMember cm
            JOIN FETCH cm.company
            WHERE d.expiryDate IS NOT NULL
            AND d.expiryDate BETWEEN :from AND :to
            """)
    List<Document> findExpiringDocuments(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
