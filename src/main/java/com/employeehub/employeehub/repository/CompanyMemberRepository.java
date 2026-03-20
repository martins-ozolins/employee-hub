package com.employeehub.employeehub.repository;

import com.employeehub.employeehub.entity.Company;
import com.employeehub.employeehub.entity.CompanyMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
    Optional<CompanyMember> findMemberByUserIdAndCompanyId(@Param("userId") UUID userId, @Param("companyId") UUID companyId);
}