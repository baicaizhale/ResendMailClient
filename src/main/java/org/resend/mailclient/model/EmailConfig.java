package org.resend.mailclient.model;

/**
 * 邮件配置模型类
 */
public class EmailConfig {
    private String apiKey;
    private String senderName;
    private String senderEmail;
    private String defaultRecipient;

    public EmailConfig() {}

    public EmailConfig(String apiKey, String senderName, String senderEmail) {
        this.apiKey = apiKey;
        this.senderName = senderName;
        this.senderEmail = senderEmail;
    }

    // Getters and Setters
    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public String getDefaultRecipient() {
        return defaultRecipient;
    }

    public void setDefaultRecipient(String defaultRecipient) {
        this.defaultRecipient = defaultRecipient;
    }
}
