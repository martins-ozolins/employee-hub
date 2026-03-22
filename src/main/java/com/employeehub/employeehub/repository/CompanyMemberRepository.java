package com.employeehub.employeehub.repository;

import com.employeehub.employeehub.entity.Company;
import com.employeehub.employeehub.entity.CompanyRole;
import com.employeehub.employeehub.entity.CompanyMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompanyMemberRepository extends JpaRepository<CompanyMember, UUID> {
    @Query("SELECT cm.company FROM CompanyMember cm WHERE cm.user.id = :userId")
    List<Company> findCompaniesByUserId(@Param("userId") UUID userId);

    @Query("SELECT cm.company FROM CompanyMember cm WHERE cm.user.id = :userId AND cm.company.id = :companyId")
    Optional<Company> findCompanyByUserIdAndCompanyId(@Param("userId") UUID userId, @Param("companyId") UUID companyId);

    @Query("SELECT cm FROM CompanyMember cm WHERE cm.user.id = :userId AND cm.company.id = :companyId")
    Optional<CompanyMember> findByUserIdAndCompanyId(@Param("userId") UUID userId, @Param("companyId") UUID companyId);

    Optional<CompanyMember> findByCompanyIdAndId(UUID companyId, UUID id);

    List<CompanyMember> findByPersonalEmailAndUserIsNull(String personalEmail);

    boolean existsByPersonalEmailAndCompany(String email, Company company);

    long countByCompanyIdAndRole(UUID companyId, CompanyRole role);

    @Query("""
            SELECT cm FROM CompanyMember cm
            WHERE cm.company.id = :companyId
            AND (:search = ''
                 OR LOWER(cm.firstName)  LIKE LOWER(CONCAT('%', :search, '%'))
                 OR LOWER(cm.lastName)   LIKE LOWER(CONCAT('%', :search, '%'))
                 OR LOWER(cm.workEmail)  LIKE LOWER(CONCAT('%', :search, '%'))
                 OR LOWER(cm.department) LIKE LOWER(CONCAT('%', :search, '%'))
                 OR LOWER(cm.jobTitle)   LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<CompanyMember> searchByCompanyId(
            @Param("companyId") UUID companyId,
            @Param("search") String search,
            Pageable pageable
    );

}