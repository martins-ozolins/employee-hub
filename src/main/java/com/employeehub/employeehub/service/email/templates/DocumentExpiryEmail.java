package com.employeehub.employeehub.service.email.templates;

import com.employeehub.employeehub.entity.Document;
import com.employeehub.employeehub.entity.CompanyMember;
import com.employeehub.employeehub.service.email.EmailTemplate;

import java.util.List;
import java.util.Map;

public class DocumentExpiryEmail implements EmailTemplate {

    private final String subject;
    private final String body;

    private DocumentExpiryEmail(String subject, String body) {
        this.subject = subject;
        this.body = body;
    }

    public static DocumentExpiryEmail forHr(String companyName, Map<CompanyMember, List<Document>> docsByMember) {
        StringBuilder body = new StringBuilder();
        body.append("The following employee documents in ").append(companyName)
            .append(" are expiring within 30 days:\n");
        for (Map.Entry<CompanyMember, List<Document>> entry : docsByMember.entrySet()) {
            CompanyMember member = entry.getKey();
            body.append("\n").append(member.getFirstName()).append(" ").append(member.getLastName()).append(":\n");
            for (Document doc : entry.getValue()) {
                body.append("  - ").append(doc.getFileName())
                    .append("  (expires ").append(doc.getExpiryDate()).append(")\n");
            }
        }
        return new DocumentExpiryEmail(
                "[EmployeeHub] Document(s) expiring soon \u2014 " + companyName,
                body.toString()
        );
    }

    @Override
    public String getSubject() {
        return subject;
    }

    @Override
    public String getBody() {
        return body;
    }
}
