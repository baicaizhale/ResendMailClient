package org.resend.mailclient.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.resend.mailclient.event.EmailSentEvent;
import org.resend.mailclient.event.StatusUpdateEvent;
import org.resend.mailclient.model.Email;
import org.resend.mailclient.model.EmailTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Resend 邮件服务类，用于处理邮件发送和保存
 */
public class ResendService {
    private static final Logger logger = LogManager.getLogger(ResendService.class);
    private static final String CONFIG_API_KEY = "resend.api.key";
    private static final String EMAILS_DIR = "emails";
    private static final String TEMPLATES_DIR = "templates";
    private static final String DRAFTS_DIR = "drafts";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    
    private final ObjectMapper objectMapper;
    private Resend resend;

    /**
     * 构造函数
     */
    public ResendService() {
        objectMapper = new ObjectMapper();
        initializeResend();
        createDirectories();
    }

    /**
     * 初始化 Resend 客户端
     */
    private void initializeResend() {
        String apiKey = ConfigService.get(CONFIG_API_KEY);
        if (!apiKey.isEmpty()) {
            resend = new Resend(apiKey);
            logger.info("Resend 客户端初始化成功");
        } else {
            logger.warn("未找到 API 密钥，请在设置中配置");
        }
    }

    /**
     * 创建必要的目录
     */
    private void createDirectories() {
        try {
            Files.createDirectories(Paths.get(EMAILS_DIR));
            Files.createDirectories(Paths.get(TEMPLATES_DIR));
            Files.createDirectories(Paths.get(DRAFTS_DIR));
            logger.info("目录创建成功");
        } catch (IOException e) {
            logger.error("创建目录失败", e);
        }
    }

    /**
     * 更新 API 密钥
     *
     * @param apiKey API 密钥
     */
    public void updateApiKey(String apiKey) {
        ConfigService.set(CONFIG_API_KEY, apiKey);
        initializeResend();
    }

    /**
     * 验证 API 密钥
     *
     * @param apiKey API 密钥
     * @return 是否有效
     */
    public boolean verifyApiKey(String apiKey) {
        try {
            Resend tempResend = new Resend(apiKey);
            // 尝试获取域名列表，如果成功则 API 密钥有效
            tempResend.domains.list();
            return true;
        } catch (ResendException e) {
            logger.error("API 密钥验证失败", e);
            return false;
        }
    }

    /**
     * 发送邮件
     *
     * @param email 邮件对象
     * @return 邮件 ID，如果发送失败则返回 null
     */
    public String sendEmail(Email email) {
        if (resend == null) {
            String errorMessage = "Resend 客户端未初始化，请先设置 API 密钥";
            logger.error(errorMessage);
            email.setStatus("失败");
            email.setErrorMessage(errorMessage);
            EventBusService.post(new EmailSentEvent(email, false));
            EventBusService.post(new StatusUpdateEvent(errorMessage));
            return null;
        }

        try {
            CreateEmailOptions options = CreateEmailOptions.builder()
                    .from(email.getFromName() + " <" + email.getFromEmail() + ">")
                    .to(email.getRecipients())
                    .subject(email.getSubject())
                    .html(email.getHtmlContent())
                    .build();

            CreateEmailResponse response = resend.emails.send(options);
            String emailId = response.getId();
            
            email.setId(emailId);
            email.setSentAt(new Date());
            email.setStatus("成功");
            
            saveEmail(email);
            EventBusService.post(new EmailSentEvent(email, true));
            EventBusService.post(new StatusUpdateEvent("邮件发送成功: " + email.getSubject()));
            
            return emailId;
        } catch (ResendException e) {
            String errorMessage = "邮件发送失败: " + e.getMessage();
            logger.error(errorMessage, e);
            
            email.setStatus("失败");
            email.setErrorMessage(errorMessage);
            
            EventBusService.post(new EmailSentEvent(email, false));
            EventBusService.post(new StatusUpdateEvent(errorMessage));
            
            return null;
        }
    }

    /**
     * 保存邮件
     *
     * @param email 邮件对象
     */
    private void saveEmail(Email email) {
        try {
            String timestamp = DATE_FORMAT.format(new Date());
            String fileName = EMAILS_DIR + File.separator + "email_" + timestamp + ".json";
            
            objectMapper.writeValue(new File(fileName), email);
            logger.info("邮件保存成功: {}", fileName);
        } catch (IOException e) {
            logger.error("保存邮件失败", e);
        }
    }

    /**
     * 保存草稿
     *
     * @param email 邮件对象
     */
    public void saveDraft(Email email) {
        try {
            String timestamp = DATE_FORMAT.format(new Date());
            String fileName = DRAFTS_DIR + File.separator + "draft_" + timestamp + ".json";
            
            objectMapper.writeValue(new File(fileName), email);
            logger.info("草稿保存成功: {}", fileName);
            EventBusService.post(new StatusUpdateEvent("草稿保存成功"));
        } catch (IOException e) {
            logger.error("保存草稿失败", e);
            EventBusService.post(new StatusUpdateEvent("草稿保存失败: " + e.getMessage()));
        }
    }

    /**
     * 保存模板
     *
     * @param template 邮件模板
     */
    public void saveTemplate(EmailTemplate template) {
        try {
            String fileName = TEMPLATES_DIR + File.separator + template.getName() + ".json";
            
            template.setUpdatedAt(new Date());
            if (template.getCreatedAt() == null) {
                template.setCreatedAt(new Date());
            }
            
            objectMapper.writeValue(new File(fileName), template);
            logger.info("模板保存成功: {}", fileName);
            EventBusService.post(new StatusUpdateEvent("模板保存成功: " + template.getName()));
        } catch (IOException e) {
            logger.error("保存模板失败", e);
            EventBusService.post(new StatusUpdateEvent("保存模板失败: " + e.getMessage()));
        }
    }

    /**
     * 加载所有邮件
     *
     * @return 邮件列表
     */
    public List<Email> loadEmails() {
        List<Email> emails = new ArrayList<>();
        File emailsDir = new File(EMAILS_DIR);
        
        if (emailsDir.exists() && emailsDir.isDirectory()) {
            File[] files = emailsDir.listFiles((dir, name) -> name.endsWith(".json"));
            if (files != null) {
                for (File file : files) {
                    try {
                        Email email = objectMapper.readValue(file, Email.class);
                        emails.add(email);
                    } catch (IOException e) {
                        logger.error("加载邮件失败: {}", file.getName(), e);
                    }
                }
            }
        }
        
        logger.info("加载了 {} 封邮件", emails.size());
        return emails;
    }

    /**
     * 加载所有模板
     *
     * @return 模板列表
     */
    public List<EmailTemplate> loadTemplates() {
        List<EmailTemplate> templates = new ArrayList<>();
        File templatesDir = new File(TEMPLATES_DIR);
        
        if (templatesDir.exists() && templatesDir.isDirectory()) {
            File[] files = templatesDir.listFiles((dir, name) -> name.endsWith(".json"));
            if (files != null) {
                for (File file : files) {
                    try {
                        EmailTemplate template = objectMapper.readValue(file, EmailTemplate.class);
                        templates.add(template);
                    } catch (IOException e) {
                        logger.error("加载模板失败: {}", file.getName(), e);
                    }
                }
            }
        }
        
        logger.info("加载了 {} 个模板", templates.size());
        return templates;
    }

    /**
     * 加载所有草稿
     *
     * @return 草稿列表
     */
    public List<Email> loadDrafts() {
        List<Email> drafts = new ArrayList<>();
        File draftsDir = new File(DRAFTS_DIR);
        
        if (draftsDir.exists() && draftsDir.isDirectory()) {
            File[] files = draftsDir.listFiles((dir, name) -> name.endsWith(".json"));
            if (files != null) {
                for (File file : files) {
                    try {
                        Email draft = objectMapper.readValue(file, Email.class);
                        drafts.add(draft);
                    } catch (IOException e) {
                        logger.error("加载草稿失败: {}", file.getName(), e);
                    }
                }
            }
        }
        
        logger.info("加载了 {} 个草稿", drafts.size());
        return drafts;
    }
}