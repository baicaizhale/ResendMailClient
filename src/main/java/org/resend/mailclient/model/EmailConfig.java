package org.resend.mailclient.model;

/**
 * 邮件配置实体类，用于存储应用程序配置信息
 */
public class EmailConfig {
    private String apiKey;
    private String senderName;
    private String senderEmail;
    private String defaultRecipient;

    /**
     * 默认构造函数
     */
    public EmailConfig() {
    }

    /**
     * 带参数的构造函数
     *
     * @param apiKey Resend API密钥
     * @param senderName 默认发件人名称
     * @param senderEmail 默认发件人邮箱
     * @param defaultRecipient 默认收件人
     */
    public EmailConfig(String apiKey, String senderName, String senderEmail, String defaultRecipient) {
        this.apiKey = apiKey;
        this.senderName = senderName;
        this.senderEmail = senderEmail;
        this.defaultRecipient = defaultRecipient;
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

    @Override
    public String toString() {
        return "EmailConfig{" +
                "senderName='" + senderName + '\'' +
                ", senderEmail='" + senderEmail + '\'' +
                ", defaultRecipient='" + defaultRecipient + '\'' +
                '}';
    }
}
