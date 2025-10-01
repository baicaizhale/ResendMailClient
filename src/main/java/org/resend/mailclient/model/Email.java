package org.resend.mailclient.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 邮件实体类，表示一封电子邮件
 */
public class Email {
    private String id;
    private String fromName;
    private String fromEmail;
    private List<String> recipients;
    private String subject;
    private String htmlContent;
    private LocalDateTime sentAt;
    private String status;
    private String errorMessage;

    /**
     * 默认构造函数
     */
    public Email() {
        this.recipients = new ArrayList<>();
        this.sentAt = LocalDateTime.now();
        this.status = "DRAFT";
    }

    /**
     * 带参数的构造函数
     *
     * @param fromName 发件人名称
     * @param fromEmail 发件人邮箱
     * @param recipients 收件人列表
     * @param subject 邮件主题
     * @param htmlContent 邮件HTML内容
     */
    public Email(String fromName, String fromEmail, List<String> recipients, String subject, String htmlContent) {
        this();
        this.fromName = fromName;
        this.fromEmail = fromEmail;
        this.recipients = recipients;
        this.subject = subject;
        this.htmlContent = htmlContent;
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

    public List<String> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<String> recipients) {
        this.recipients = recipients;
    }

    /**
     * 获取格式化的收件人列表，用于UI显示
     *
     * @return 以分号分隔的收件人列表
     */
    public String getFormattedRecipients() {
        return recipients == null ? "" : String.join("; ", recipients);
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

    @Override
    public String toString() {
        return "Email{" +
                "id='" + id + '\'' +
                ", subject='" + subject + '\'' +
                ", status='" + status + '\'' +
                ", sentAt=" + sentAt +
                '}';
    }
}
