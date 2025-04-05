package org.resend.mailclient.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.SendEmailRequest;
import com.resend.services.emails.model.SendEmailResponse;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class ResendService {
    private String apiKey;
    private String defaultFrom;

    public ResendService() {
        loadConfig();
    }

    private void loadConfig() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            Properties prop = new Properties();
            prop.load(input);

            apiKey = prop.getProperty("api.key");
            defaultFrom = prop.getProperty("default.from");

            if (apiKey == null || apiKey.isEmpty()) {
                throw new RuntimeException("API密钥(api.key)未配置");
            }
            if (defaultFrom == null || defaultFrom.isEmpty()) {
                throw new RuntimeException("默认发件邮箱(default.from)未配置");
            }

            System.out.println("配置加载成功: apiKey=" + maskApiKey(apiKey) + ", defaultFrom=" + defaultFrom);

        } catch (Exception ex) {
            throw new RuntimeException("加载配置失败: " + ex.getMessage(), ex);
        }
    }

    private String maskApiKey(String key) {
        if (key == null || key.length() <= 8) return "****";
        return key.substring(0, 3) + "..." + key.substring(key.length() - 3);
    }

    public void sendEmail(String fromName, String fromEmail, String to, String subject, String html)
            throws ResendException {

        // 验证发件邮箱域名
        if (!fromEmail.contains("@")) {
            throw new IllegalArgumentException("发件邮箱格式错误: " + fromEmail);
        }

        // 构建RFC 5322标准发件人
        String formattedFrom = String.format("%s <%s>", fromName.trim(), fromEmail);

        // 处理多收件人
        List<String> recipients = Arrays.asList(to.split("\\s*;\\s*"));

        Resend resend = new Resend(apiKey);

        SendEmailRequest request = SendEmailRequest.builder()
                .from(formattedFrom)
                .to(recipients)
                .subject(subject)
                .html(html)
                .build();

        System.out.println("[DEBUG] 发送请求: " + request);

        SendEmailResponse response = resend.emails().send(request);
        System.out.println("邮件发送成功，ID: " + response.getId());
    }
}