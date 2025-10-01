package org.resend.mailclient.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 邮件模型类
 */
public class Email {
    private String id;
    private String fromName;
    private String fromEmail;
    private List<String> to;
    private String subject;
    private String htmlContent;
    private LocalDateTime sentAt;
    private String status;
    private String errorMessage;

    public Email() {}

    public Email(String fromName, String fromEmail, List<String> to, String subject, String htmlContent) {
        this.fromName = fromName;
        this.fromEmail = fromEmail;
        this.to = to;
        this.subject = subject;
        this.htmlContent = htmlContent;
        this.sentAt = LocalDateTime.now();
        this.status = "DRAFT"; // DRAFT, SENDING, SENT, FAILED
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String getFromEmail() {
        return fromEmail;
    }

    public void setFromEmail(String fromEmail) {
        this.fromEmail = fromEmail;
    }

    public List<String> getTo() {
        return to;
    }

    public void setTo(List<String> to) {
        this.to = to;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getHtmlContent() {
        return htmlContent;
    }

    public void setHtmlContent(String htmlContent) {
        this.htmlContent = htmlContent;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
