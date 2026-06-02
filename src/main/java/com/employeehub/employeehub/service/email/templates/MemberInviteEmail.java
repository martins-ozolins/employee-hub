package com.employeehub.employeehub.service.email.templates;

import com.employeehub.employeehub.service.email.EmailTemplate;

public class MemberInviteEmail implements EmailTemplate {

    private final String companyName;
    private final String memberName;
    private final String baseUrl;

    public MemberInviteEmail(String companyName, String memberName, String baseUrl) {
        this.companyName = companyName;
        this.memberName = memberName;
        this.baseUrl = baseUrl + "/auth/login";
    }

    @Override
    public String getSubject() {
        return "You've been added to " + companyName + " on EmployeeHub";
    }

    @Override
    public String getBody() {
        return "Hi " + memberName + ",\n\n"
                + "You've been added as an employee of " + companyName + " on EmployeeHub.\n\n"
                + "Log in or create an account to access your profile: " + baseUrl;
    }
}
