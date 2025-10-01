package org.resend.mailclient.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 邮件模板实体类，用于存储和管理邮件模板
 */
public class EmailTemplate {
    private String id;
    private String name;
    private String subject;
    private String htmlContent;
    private List<String> recipients;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 默认构造函数
     */
    public EmailTemplate() {
        this.recipients = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 带参数的构造函数
     *
     * @param name 模板名称
     * @param subject 邮件主题
     * @param htmlContent 邮件HTML内容
     */
    public EmailTemplate(String name, String subject, String htmlContent) {
        this();
        this.name = name;
        this.subject = subject;
        this.htmlContent = htmlContent;
    }

    /**
     * 完整参数的构造函数
     *
     * @param name 模板名称
     * @param subject 邮件主题
     * @param htmlContent 邮件HTML内容
     * @param recipients 收件人列表
     */
    public EmailTemplate(String name, String subject, String htmlContent, List<String> recipients) {
        this(name, subject, htmlContent);
        this.recipients = recipients;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.updatedAt = LocalDateTime.now();
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
        this.updatedAt = LocalDateTime.now();
    }

    public String getHtmlContent() {
        return htmlContent;
    }

    public void setHtmlContent(String htmlContent) {
        this.htmlContent = htmlContent;
        this.updatedAt = LocalDateTime.now();
    }

    public List<String> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<String> recipients) {
        this.recipients = recipients;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 获取格式化的收件人列表，用于UI显示
     *
     * @return 以分号分隔的收件人列表
     */
    public String getFormattedRecipients() {
        return recipients == null ? "" : String.join("; ", recipients);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return name;
    }
}
