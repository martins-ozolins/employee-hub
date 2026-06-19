# Refactor Plan: Feature-Based Package Structure

## Context

The current project uses a flat, layer-first layout (`controller/`, `service/`, `entity/`, etc.) with 106 files in 13 packages. This refactor mirrors the structure from the Ad-spotter project — `features/`, `security/`, and `shared/` top-level packages — grouping related code (controller + service + entity + repo + dto) together per domain.

This is a **pure structural refactor** — no logic changes, only package declarations and imports update.

---

## Target Structure

```
com.employeehub.employeehub/
├── EmployeeHubApplication.java
│
├── config/                             (app-wide Spring config beans — unchanged)
│   ├── AsyncConfig.java
│   ├── DataSeeder.java
│   ├── JwtProperties.java
│   ├── RabbitMqConfig.java
│   ├── S3Config.java
│   ├── SchedulingConfig.java
│   └── WebConfig.java
│
├── features/
│   ├── auth/
│   │   ├── controller/  AuthController.java
│   │   ├── dto/         AuthDtos.java
│   │   ├── entity/      User.java, RefreshToken.java, VerificationToken.java,
│   │   │                TokenType.java, PlatformRole.java
│   │   ├── job/         TokenCleanupJob.java
│   │   ├── repository/  UserRepository.java, RefreshTokenRepository.java,
│   │   │                VerificationTokenRepository.java
│   │   └── service/     AuthService.java, VerificationTokenService.java
│   │
│   ├── company/
│   │   ├── controller/  CompanyController.java
│   │   ├── dto/         CompanyDtos.java
│   │   ├── entity/      Company.java
│   │   ├── repository/  CompanyRepository.java
│   │   └── service/     CompanyService.java
│   │
│   ├── members/
│   │   ├── controller/  CompanyMemberController.java, SalaryController.java,
│   │   │                JobTitleController.java
│   │   ├── dto/         CompanyMemberDtos.java, SalaryDtos.java, JobTitleDtos.java
│   │   ├── entity/      CompanyMember.java, SalaryRecord.java, JobTitleRecord.java,
│   │   │                EmploymentStatus.java, TitleChangeType.java
│   │   ├── repository/  CompanyMemberRepository.java, SalaryRecordRepository.java,
│   │   │                JobTitleRecordRepository.java
│   │   ├── service/     CompanyMemberService.java, SalaryService.java, JobTitleService.java
│   │   └── util/        CompanyMemberUtils.java, SalaryUtils.java, JobTitleUtils.java
│   │
│   ├── documents/
│   │   ├── controller/  DocumentStorageController.java
│   │   ├── dto/         DocumentDtos.java
│   │   ├── entity/      Document.java
│   │   ├── job/         DocumentExpiryNotificationJob.java
│   │   ├── repository/  DocumentRepository.java
│   │   ├── service/     DocumentStorageService.java
│   │   └── util/        DocumentUtils.java
│   │
│   ├── roles/
│   │   ├── controller/  CompanyPermissionController.java, CompanyRoleController.java
│   │   ├── dto/         CompanyRoleDtos.java
│   │   ├── entity/      CompanyPermission.java, CompanyPermissionEntity.java,
│   │   │                CompanyRoleEntity.java, DefaultCompanyRole.java
│   │   ├── repository/  CompanyPermissionRepository.java, CompanyRoleRepository.java
│   │   ├── service/     CompanyPermissionService.java, CompanyRoleService.java
│   │   └── util/        CompanyRoleUtils.java
│   │
│   ├── selfservice/
│   │   └── controller/  SelfServiceController.java
│   │                    (reuses services from members/ and documents/)
│   │
│   └── email/
│       ├── event/       EmailEvent.java, EmailEventListener.java,
│       │                EmailEventPublisher.java, EmailEventType.java
│       ├── service/     EmailSender.java, EmailTemplate.java,
│       │                LogEmailService.java, SmtpEmailService.java
│       └── template/    DocumentExpiryEmail.java, EmailVerificationEmail.java,
│                        MemberInviteEmail.java, PasswordChangedEmail.java,
│                        PasswordResetCompleteEmail.java, PasswordResetEmail.java,
│                        PrerenderedEmail.java
│
├── security/
│   ├── config/          AppSecurityConfig.java
│   ├── filter/          JwtCookieAuthFilter.java
│   ├── model/           AppUserDetails.java, AppUserDetailsService.java
│   └── service/         JwtService.java
│
└── shared/
    ├── dto/             ApiResponses.java, ErrorResponse.java
    ├── exception/       BadRequestException.java, ConflictException.java,
    │                    EmailAlreadyUsedException.java, EmailNotVerifiedException.java,
    │                    ForbiddenException.java, GlobalExceptionHandler.java,
    │                    InvalidCredentialsException.java, NotFoundException.java
    └── util/            CookieUtils.java
```

---

## Key Mapping Decisions

| Current package | New package |
|---|---|
| `controller/AuthController` | `features/auth/controller/` |
| `service/JwtService` | `security/service/` (infrastructure, not domain) |
| `security/JwtCookieAuthFilter` | `security/filter/` |
| `config/AppSecurityConfig` | `security/config/` |
| `config/AppUserDetails` | `security/model/` |
| `config/AppUserDetailsService` | `security/model/` |
| `config/{Async,Web,Rabbit,S3,Scheduling,DataSeeder,JwtProperties}` | `config/` (unchanged) |
| `exception/*` | `shared/exception/` |
| `dto/ApiResponses`, `dto/ErrorResponse` | `shared/dto/` |
| `util/CookieUtils` | `shared/util/` |
| `event/*` | `features/email/event/` |
| `service/email/*` | `features/email/service/` |
| `service/email/templates/*` | `features/email/template/` |
| `job/TokenCleanupJob` | `features/auth/job/` |
| `job/DocumentExpiryNotificationJob` | `features/documents/job/` |

---

## Implementation Order

Move files in this order to minimize cascading broken imports:

1. **shared/** — exceptions, shared DTOs, CookieUtils (most imported, no app dependencies)
2. **security/** — AppUserDetails, AppUserDetailsService, AppSecurityConfig, JwtCookieAuthFilter, JwtService
3. **features/email/** — event classes, email services, templates
4. **features/auth/** — User/token entities, repos, AuthService, VerificationTokenService, AuthController, TokenCleanupJob
5. **features/roles/** — role/permission entities, repos, services, controllers
6. **features/company/** — Company entity, repo, service, controller
7. **features/members/** — member entities, repos, services, controllers, utils
8. **features/documents/** — Document entity, repo, service, controller, util, job
9. **features/selfservice/** — SelfServiceController

For each file: update `package` declaration → update `import` statements → delete old file.

---

## Verification

```bash
./mvnw compile    # must pass with zero errors
```

Then spot-check old packages are gone:
```bash
grep -r "com.employeehub.employeehub.controller" src/   # should be empty
grep -r "com.employeehub.employeehub.service" src/      # should be empty
grep -r "com.employeehub.employeehub.entity" src/       # should be empty
```

Finally, boot the app and hit `POST /auth/login` to confirm the Spring context loads and JWT security filter works correctly.