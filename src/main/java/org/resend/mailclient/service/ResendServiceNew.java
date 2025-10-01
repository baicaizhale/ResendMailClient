package org.resend.mailclient.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.SendEmailRequest;
import com.resend.services.emails.model.SendEmailResponse;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.greenrobot.eventbus.EventBus;
import org.resend.mailclient.model.Email;
import org.resend.mailclient.model.EmailTemplate;
import org.resend.mailclient.service.events.EmailSentEvent;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Resend邮件服务类，处理与Resend API的交互
 */
public class ResendServiceNew {
    private static final Logger logger = LogManager.getLogger(ResendServiceNew.class);
    private static final String CONFIG_DIR = "mailclient";
    private static final String TEMPLATES_DIR = CONFIG_DIR + File.separator + "templates";
    private static final String HISTORY_DIR = CONFIG_DIR + File.separator + "history";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    private Resend resend;

    /**
     * 构造函数，初始化Resend客户端
     */
    public ResendServiceNew() {
        // 确保目录存在
        ensureDirectoriesExist();
    }

    /**
     * 初始化或更新Resend客户端
     * @param apiKey Resend API密钥
     */
    public void initializeResend(String apiKey) {
        if (apiKey != null && !apiKey.isEmpty()) {
            this.resend = new Resend(apiKey);
            logger.info("Resend客户端已初始化");
        } else {
            this.resend = null;
            logger.warn("API密钥未设置，Resend客户端未初始化");
        }
    }

    /**
     * 验证API密钥是否有效
     * @param apiKey API密钥
     * @return 是否有效
     */
    public boolean verifyApiKey(String apiKey) {
        try {
            Resend tempResend = new Resend(apiKey);
            // 尝试获取API密钥信息，如果成功则有效
            tempResend.api().get();
            return true;
        } catch (Exception e) {
            logger.error("API密钥验证失败", e);
            return false;
        }
    }

    /**
     * 发送邮件
     * @param fromName 发件人名称
     * @param fromEmail 发件人邮箱
     * @param to 收件人列表，用分号分隔
     * @param subject 邮件主题
     * @param html 邮件HTML内容
     * @return 邮件ID
     * @throws ResendException 发送失败时抛出
     */
    public String sendEmail(String fromName, String fromEmail, String to, String subject, String html) throws ResendException {
        return sendEmail(fromName, fromEmail, to, subject, html, null);
    }

    /**
     * 发送邮件
     * @param fromName 发件人名称
     * @param fromEmail 发件人邮箱
     * @param to 收件人列表，用分号分隔
     * @param subject 邮件主题
     * @param html 邮件HTML内容
     * @param attachments 附件列表
     * @return 邮件ID
     * @throws ResendException 发送失败时抛出
     */
    public String sendEmail(String fromName, String fromEmail, String to, String subject, String html, List<File> attachments) throws ResendException {
        if (resend == null) {
            throw new ResendException("API密钥未设置，无法发送邮件");
        }

        validateEmail(fromEmail);
        List<String> recipients = parseRecipients(to);

        SendEmailRequest.Builder requestBuilder = SendEmailRequest.builder()
                .from(formatFrom(fromName, fromEmail))
                .to(recipients)
                .subject(subject)
                .html(html);

        // 如果有附件，添加到请求中
        if (attachments != null && !attachments.isEmpty()) {
            // 注意：Resend Java SDK可能不支持附件，这里只是示例代码
            // 实际实现可能需要使用其他方式处理附件
            logger.warn("当前版本不支持附件功能");
        }

        SendEmailRequest request = requestBuilder.build();
        SendEmailResponse response = resend.emails().send(request);

        // 保存邮件到历史记录
        saveToHistory(fromName, fromEmail, recipients, subject, html, response.getId(), "SENT");

        logger.info("邮件发送成功，ID: {}", response.getId());
        EventBus.getDefault().post(new EmailSentEvent(createEmailModel(fromName, fromEmail, recipients, subject, html, response.getId(), "SENT"), true));

        return response.getId();
    }

    /**
     * 保存邮件草稿
     * @param email 邮件对象
     */
    public void saveDraft(Email email) {
        try {
            String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
            String fileName = "draft_" + timestamp + ".json";
            Path draftPath = Paths.get(HISTORY_DIR, fileName);

            objectMapper.writeValue(draftPath.toFile(), email);
            logger.info("邮件草稿已保存: {}", fileName);
        } catch (IOException e) {
            logger.error("保存邮件草稿失败", e);
        }
    }

    /**
     * 获取邮件历史记录
     * @return 邮件历史记录列表
     */
    public List<Email> getEmailHistory() {
        List<Email> history = new ArrayList<>();
        try {
            if (!Files.exists(Paths.get(HISTORY_DIR))) {
                return history;
            }

            Files.list(Paths.get(HISTORY_DIR))
                .filter(path -> path.toString().endsWith(".json"))
                .forEach(path -> {
                    try {
                        Email email = objectMapper.readValue(path.toFile(), Email.class);
                        history.add(email);
                    } catch (IOException e) {
                        logger.error("读取邮件历史记录失败: " + path, e);
                    }
                });

            // 按时间倒序排列
            history.sort((e1, e2) -> e2.getSentAt().compareTo(e1.getSentAt()));
        } catch (IOException e) {
            logger.error("获取邮件历史记录失败", e);
        }
        return history;
    }

    /**
     * 清空邮件历史记录
     */
    public void clearEmailHistory() {
        try {
            if (Files.exists(Paths.get(HISTORY_DIR))) {
                Files.list(Paths.get(HISTORY_DIR))
                    .filter(path -> path.toString().endsWith(".json"))
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            logger.error("删除邮件历史记录失败: " + path, e);
                        }
                    });
                logger.info("邮件历史记录已清空");
            }
        } catch (IOException e) {
            logger.error("清空邮件历史记录失败", e);
        }
    }

    /**
     * 保存邮件模板
     * @param template 邮件模板
     */
    public void saveTemplate(EmailTemplate template) {
        try {
            String fileName = template.getId() + ".json";
            Path templatePath = Paths.get(TEMPLATES_DIR, fileName);

            objectMapper.writeValue(templatePath.toFile(), template);
            logger.info("邮件模板已保存: {}", template.getName());
        } catch (IOException e) {
            logger.error("保存邮件模板失败", e);
        }
    }

    /**
     * 获取所有邮件模板
     * @return 邮件模板列表
     */
    public List<EmailTemplate> getTemplates() {
        List<EmailTemplate> templates = new ArrayList<>();
        try {
            if (!Files.exists(Paths.get(TEMPLATES_DIR))) {
                return templates;
            }

            Files.list(Paths.get(TEMPLATES_DIR))
                .filter(path -> path.toString().endsWith(".json"))
                .forEach(path -> {
                    try {
                        EmailTemplate template = objectMapper.readValue(path.toFile(), EmailTemplate.class);
                        templates.add(template);
                    } catch (IOException e) {
                        logger.error("读取邮件模板失败: " + path, e);
                    }
                });
        } catch (IOException e) {
            logger.error("获取邮件模板失败", e);
        }
        return templates;
    }

    /**
     * 删除邮件模板
     * @param templateId 模板ID
     */
    public void deleteTemplate(String templateId) {
        try {
            Path templatePath = Paths.get(TEMPLATES_DIR, templateId + ".json");
            if (Files.exists(templatePath)) {
                Files.delete(templatePath);
                logger.info("邮件模板已删除: {}", templateId);
            }
        } catch (IOException e) {
            logger.error("删除邮件模板失败", e);
        }
    }

    // ========== 私有方法 ==========

    /**
     * 确保必要的目录存在
     */
    private void ensureDirectoriesExist() {
        try {
            Files.createDirectories(Paths.get(CONFIG_DIR));
            Files.createDirectories(Paths.get(TEMPLATES_DIR));
            Files.createDirectories(Paths.get(HISTORY_DIR));
        } catch (IOException e) {
            logger.error("创建目录失败", e);
        }
    }

    /**
     * 解析收件人列表
     * @param to 收件人字符串，多个收件人用分号分隔
     * @return 收件人列表
     */
    private List<String> parseRecipients(String to) {
        return Arrays.stream(to.split(";"))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toList());
    }

    /**
     * 构建发件人字符串
     * @param name 发件人名称
     * @param email 发件人邮箱
     * @return 格式化后的发件人字符串
     */
    private String formatFrom(String name, String email) {
        return String.format("%s <%s>", name.trim(), email.trim());
    }

    /**
     * 验证邮箱格式
     * @param email 邮箱地址
     * @throws IllegalArgumentException 如果邮箱格式无效
     */
    private void validateEmail(String email) {
        if (!email.matches("^[\w-.]+@([\w-]+\.)+[\w-]{2,4}$")) {
            throw new IllegalArgumentException("邮箱格式无效: " + email);
        }
    }

    /**
     * 创建邮件模型对象
     * @param fromName 发件人名称
     * @param fromEmail 发件人邮箱
     * @param to 收件人列表
     * @param subject 邮件主题
     * @param html 邮件内容
     * @param id 邮件ID
     * @param status 邮件状态
     * @return 邮件模型对象
     */
    private Email createEmailModel(String fromName, String fromEmail, List<String> to, String subject, String html, String id, String status) {
        Email email = new Email(fromName, fromEmail, to, subject, html);
        email.setId(id);
        email.setStatus(status);
        email.setSentAt(LocalDateTime.now());
        return email;
    }

    /**
     * 保存邮件到历史记录
     * @param fromName 发件人名称
     * @param fromEmail 发件人邮箱
     * @param to 收件人列表
     * @param subject 邮件主题
     * @param html 邮件内容
     * @param emailId 邮件ID
     * @param status 邮件状态
     */
    private void saveToHistory(String fromName, String fromEmail, List<String> to, String subject, String html, String emailId, String status) {
        Email email = createEmailModel(fromName, fromEmail, to, subject, html, emailId, status);

        try {
            String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
            String fileName = "email_" + timestamp + ".json";
            Path emailPath = Paths.get(HISTORY_DIR, fileName);

            objectMapper.writeValue(emailPath.toFile(), email);
            logger.info("邮件已保存到历史记录: {}", fileName);
        } catch (IOException e) {
            logger.error("保存邮件到历史记录失败", e);
        }
    }
}
