package com.employeehub.employeehub.job;

import com.employeehub.employeehub.entity.CompanyMember;
import com.employeehub.employeehub.entity.Document;
import com.employeehub.employeehub.entity.Permission;
import com.employeehub.employeehub.repository.CompanyMemberRepository;
import com.employeehub.employeehub.repository.DocumentRepository;
import com.employeehub.employeehub.service.email.EmailSender;
import com.employeehub.employeehub.service.email.templates.DocumentExpiryEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class DocumentExpiryNotificationJob {

    private static final Logger log = LoggerFactory.getLogger(DocumentExpiryNotificationJob.class);
    private static final int NOTIFY_DAYS_AHEAD = 30;

    private final DocumentRepository documentRepository;
    private final CompanyMemberRepository companyMemberRepository;
    private final EmailSender emailSender;

    public DocumentExpiryNotificationJob(DocumentRepository documentRepository,
                                         CompanyMemberRepository companyMemberRepository,
                                         EmailSender emailSender) {
        this.documentRepository = documentRepository;
        this.companyMemberRepository = companyMemberRepository;
        this.emailSender = emailSender;
    }

    @Scheduled(cron = "0 0 12 * * *")
    @Transactional(readOnly = true)
    public void notifyExpiringDocuments() {
        LocalDate today = LocalDate.now();
        List<Document> expiring = documentRepository.findExpiringDocuments(today, today.plusDays(NOTIFY_DAYS_AHEAD));

        if (expiring.isEmpty()) {
            log.info("No expiring documents found — skipping notifications");
            return;
        }

        // Group by company
        Map<UUID, List<Document>> byCompany = expiring.stream()
                .collect(Collectors.groupingBy(d -> d.getCompanyMember().getCompany().getId()));

        for (Map.Entry<UUID, List<Document>> companyEntry : byCompany.entrySet()) {
            UUID companyId = companyEntry.getKey();
            List<Document> companyDocs = companyEntry.getValue();
            String companyName = companyDocs.get(0).getCompanyMember().getCompany().getName();

            // Group by member (preserving insertion order for consistent email output)
            Map<CompanyMember, List<Document>> byMember = companyDocs.stream()
                    .collect(Collectors.groupingBy(Document::getCompanyMember, LinkedHashMap::new, Collectors.toList()));

            // Notify each affected employee
            for (Map.Entry<CompanyMember, List<Document>> memberEntry : byMember.entrySet()) {
                CompanyMember member = memberEntry.getKey();
                if (member.getWorkEmail() != null && !member.getWorkEmail().isBlank()) {
                    emailSender.send(
                            member.getWorkEmail(),
                            DocumentExpiryEmail.forEmployee(member.getFirstName(), memberEntry.getValue())
                    );
                }
            }

            // Notify anyone with MANAGE_DOCUMENTS permission with a consolidated summary
            List<CompanyMember> hrMembers = companyMemberRepository.findByCompanyIdAndPermission(
                    companyId, Permission.MANAGE_DOCUMENTS.name()
            );
            for (CompanyMember hr : hrMembers) {
                if (hr.getWorkEmail() != null && !hr.getWorkEmail().isBlank()) {
                    emailSender.send(
                            hr.getWorkEmail(),
                            DocumentExpiryEmail.forHr(companyName, byMember)
                    );
                }
            }
        }

        log.info("Document expiry notifications sent for {} document(s) across {} company/companies",
                expiring.size(), byCompany.size());
    }
}
