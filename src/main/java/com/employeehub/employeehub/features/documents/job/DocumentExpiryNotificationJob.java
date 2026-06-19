package com.employeehub.employeehub.features.documents.job;

import com.employeehub.employeehub.features.documents.entity.Document;
import com.employeehub.employeehub.features.documents.repository.DocumentRepository;
import com.employeehub.employeehub.features.email.event.EmailEvent;
import com.employeehub.employeehub.features.email.event.EmailEventPublisher;
import com.employeehub.employeehub.features.email.event.EmailEventType;
import com.employeehub.employeehub.features.email.template.DocumentExpiryEmail;
import com.employeehub.employeehub.features.members.entity.CompanyMember;
import com.employeehub.employeehub.features.members.repository.CompanyMemberRepository;
import com.employeehub.employeehub.features.roles.entity.CompanyPermission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class DocumentExpiryNotificationJob {

    private static final Logger log = LoggerFactory.getLogger(DocumentExpiryNotificationJob.class);
    private static final int NOTIFY_DAYS_AHEAD = 30;

    private final DocumentRepository documentRepository;
    private final CompanyMemberRepository companyMemberRepository;
    private final EmailEventPublisher emailEventPublisher;

    public DocumentExpiryNotificationJob(DocumentRepository documentRepository,
                                         CompanyMemberRepository companyMemberRepository,
                                         EmailEventPublisher emailEventPublisher) {
        this.documentRepository = documentRepository;
        this.companyMemberRepository = companyMemberRepository;
        this.emailEventPublisher = emailEventPublisher;
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

            // Notify anyone with MANAGE_DOCUMENTS permission with a consolidated summary
            List<CompanyMember> hrMembers = companyMemberRepository.findByCompanyIdAndPermission(
                    companyId, CompanyPermission.MANAGE_DOCUMENTS.name()
            );
            if (hrMembers.isEmpty()) continue;

            Map<CompanyMember, List<Document>> byMember = companyDocs.stream()
                    .collect(Collectors.groupingBy(Document::getCompanyMember, LinkedHashMap::new, Collectors.toList()));
            DocumentExpiryEmail emailTemplate = DocumentExpiryEmail.forHr(companyName, byMember);
            Map<String, String> data = Map.of("subject", emailTemplate.getSubject(), "body", emailTemplate.getBody());

            for (CompanyMember hr : hrMembers) {
                if (hr.getWorkEmail() != null && !hr.getWorkEmail().isBlank()) {
                    emailEventPublisher.publish(new EmailEvent(EmailEventType.DOCUMENT_EXPIRY_HR, hr.getWorkEmail(), data));
                }
            }
        }

        log.info("Document expiry notifications sent for {} document(s) across {} company/companies",
                expiring.size(), byCompany.size());
    }
}
