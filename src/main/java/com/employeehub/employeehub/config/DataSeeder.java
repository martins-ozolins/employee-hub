package com.employeehub.employeehub.config;

import com.employeehub.employeehub.entity.*;
import com.employeehub.employeehub.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@Profile("local")
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final CompanyRoleRepository companyRoleRepository;
    private final CompanyPermissionRepository companyPermissionRepository;
    private final CompanyMemberRepository companyMemberRepository;
    private final SalaryRecordRepository salaryRecordRepository;
    private final JobTitleRecordRepository jobTitleRecordRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(
            UserRepository userRepository,
            CompanyRepository companyRepository,
            CompanyRoleRepository companyRoleRepository,
            CompanyPermissionRepository companyPermissionRepository,
            CompanyMemberRepository companyMemberRepository,
            SalaryRecordRepository salaryRecordRepository,
            JobTitleRecordRepository jobTitleRecordRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.companyRoleRepository = companyRoleRepository;
        this.companyPermissionRepository = companyPermissionRepository;
        this.companyMemberRepository = companyMemberRepository;
        this.salaryRecordRepository = salaryRecordRepository;
        this.jobTitleRecordRepository = jobTitleRecordRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.findByEmail("admin@example.com").isPresent()) {
            log.info("Seed data already present — skipping");
            return;
        }

        log.info("Seeding development data...");

        // --- User ---
        User user = userRepository.save(User.builder()
                .email("admin@example.com")
                .passwordHash(passwordEncoder.encode("password"))
                .firstName("Alice")
                .lastName("Smith")
                .isActive(true)
                .emailVerified(true)
                .role(PlatformRole.USER)
                .build());

        // --- Company ---
        Company company = companyRepository.save(Company.builder()
                .name("Acme Corp")
                .industry("Technology")
                .location("Riga, Latvia")
                .description("A demo company for local development")
                .build());

        // --- Permissions ---
        List<CompanyPermissionEntity> allPermissions = companyPermissionRepository.findAll();
        Set<CompanyPermissionEntity> allPermsSet = new HashSet<>(allPermissions);

        Set<CompanyPermissionEntity> hrPerms = new HashSet<>(companyPermissionRepository.findAllByNameIn(List.of(
                "VIEW_MEMBERS", "VIEW_MEMBER_DETAILS", "MANAGE_MEMBERS",
                "MANAGE_SALARY", "MANAGE_JOB_TITLES", "MANAGE_DOCUMENTS"
        )));

        Set<CompanyPermissionEntity> managerPerms = new HashSet<>(companyPermissionRepository.findAllByNameIn(List.of(
                "VIEW_MEMBERS", "VIEW_MEMBER_DETAILS"
        )));

        Set<CompanyPermissionEntity> employeePerms = new HashSet<>(companyPermissionRepository.findAllByNameIn(List.of(
                "VIEW_MEMBERS"
        )));

        // --- Roles ---
        CompanyRoleEntity ownerRole = companyRoleRepository.save(CompanyRoleEntity.builder()
                .company(company).name("WORKSPACE_OWNER").isSystem(true).permissions(allPermsSet).build());

        CompanyRoleEntity hrRole = companyRoleRepository.save(CompanyRoleEntity.builder()
                .company(company).name("HR").isSystem(false).permissions(hrPerms).build());

        CompanyRoleEntity managerRole = companyRoleRepository.save(CompanyRoleEntity.builder()
                .company(company).name("Manager").isSystem(false).permissions(managerPerms).build());

        CompanyRoleEntity employeeRole = companyRoleRepository.save(CompanyRoleEntity.builder()
                .company(company).name("Employee").isSystem(false).permissions(employeePerms).build());

        // --- Owner member (linked to user) ---
        CompanyMember owner = companyMemberRepository.save(CompanyMember.builder()
                .user(user)
                .company(company)
                .companyRole(ownerRole)
                .firstName("Alice")
                .lastName("Smith")
                .personalEmail("admin@example.com")
                .workEmail("alice.smith@acme.com")
                .department("Management")
                .jobTitle("CEO")
                .joinDate(LocalDate.of(2022, 1, 1))
                .employmentStatus(EmploymentStatus.ACTIVE)
                .selfServiceEnabled(true)
                .currentSalaryAmount(new BigDecimal("8000.00"))
                .currentSalaryCurrency("EUR")
                .build());

        salaryRecordRepository.save(SalaryRecord.builder()
                .companyMember(owner)
                .amount(new BigDecimal("8000.00"))
                .currency("EUR")
                .effectiveDate(LocalDate.of(2022, 1, 1))
                .build());

        jobTitleRecordRepository.save(JobTitleRecord.builder()
                .companyMember(owner)
                .jobTitle("CEO")
                .changeType(TitleChangeType.INITIAL)
                .effectiveDate(LocalDate.of(2022, 1, 1))
                .build());

        // --- Extra members (no user, no self-service) ---
        record MemberSeed(String first, String last, String email, String dept, String title,
                          CompanyRoleEntity role, BigDecimal salary, String currency) {}

        List<MemberSeed> seeds = List.of(
                new MemberSeed("Bob",    "Johnson", "bob.johnson@acme.com",    "HR",          "HR Manager",        hrRole,       new BigDecimal("5500.00"), "EUR"),
                new MemberSeed("Carol",  "Williams","carol.williams@acme.com", "Engineering", "Software Engineer", employeeRole, new BigDecimal("4800.00"), "EUR"),
                new MemberSeed("David",  "Brown",   "david.brown@acme.com",    "Engineering", "Team Lead",         managerRole,  new BigDecimal("6200.00"), "EUR"),
                new MemberSeed("Emma",   "Davis",   "emma.davis@acme.com",     "Marketing",   "Marketing Specialist", employeeRole, new BigDecimal("4200.00"), "EUR"),
                new MemberSeed("Frank",  "Miller",  "frank.miller@acme.com",   "Finance",     "Accountant",        employeeRole, new BigDecimal("4500.00"), "EUR")
        );

        for (MemberSeed s : seeds) {
            LocalDate joinDate = LocalDate.of(2023, 3, 1);
            CompanyMember member = companyMemberRepository.save(CompanyMember.builder()
                    .user(null)
                    .company(company)
                    .companyRole(s.role())
                    .firstName(s.first())
                    .lastName(s.last())
                    .personalEmail(s.email())
                    .workEmail(s.email())
                    .department(s.dept())
                    .jobTitle(s.title())
                    .joinDate(joinDate)
                    .employmentStatus(EmploymentStatus.ACTIVE)
                    .selfServiceEnabled(false)
                    .currentSalaryAmount(s.salary())
                    .currentSalaryCurrency(s.currency())
                    .build());

            salaryRecordRepository.save(SalaryRecord.builder()
                    .companyMember(member)
                    .amount(s.salary())
                    .currency(s.currency())
                    .effectiveDate(joinDate)
                    .build());

            jobTitleRecordRepository.save(JobTitleRecord.builder()
                    .companyMember(member)
                    .jobTitle(s.title())
                    .changeType(TitleChangeType.INITIAL)
                    .effectiveDate(joinDate)
                    .build());
        }

        log.info("Seed complete: 1 user, 1 company, 4 roles, 6 members");
    }
}