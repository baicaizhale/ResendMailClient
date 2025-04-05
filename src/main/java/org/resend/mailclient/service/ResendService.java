package org.resend.mailclient.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.SendEmailRequest;
import com.resend.services.emails.model.SendEmailResponse;
import java.util.Arrays;
import java.util.List;

public class ResendService {
    public void sendEmail(String fromName, String fromEmail, String to, String subject, String html) throws ResendException {
        validateEmail(fromEmail);
        List<String> recipients = parseRecipients(to);

        Resend resend = new Resend(ConfigManager.get("api.key"));
        SendEmailRequest request = buildRequest(fromName, fromEmail, recipients, subject, html);

        SendEmailResponse response = resend.emails().send(request);
        System.out.println("邮件发送成功，ID: " + response.getId());
    }

    private List<String> parseRecipients(String to) {
        return Arrays.asList(to.split(";"));
    }

    private SendEmailRequest buildRequest(String fromName, String fromEmail, List<String> to, String subject, String html) {
        return SendEmailRequest.builder()
                .from(formatFrom(fromName, fromEmail))
                .to(to)
                .subject(subject)
                .html(html)
                .build();
    }

    private String formatFrom(String name, String email) {
        return String.format("%s <%s>", name.trim(), email.trim());
    }

    private void validateEmail(String email) {
        if (!email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            throw new IllegalArgumentException("邮箱格式无效: " + email);
        }
    }
}