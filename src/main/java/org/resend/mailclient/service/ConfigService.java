package org.resend.mailclient.service;

import java.io.*;
import java.util.Properties;

public class ConfigService {
    private static final String CONFIG_FILE = "config.properties";

    public void saveConfig(String apiKey, String defaultFrom, String senderName) {
        Properties prop = new Properties();
        prop.setProperty("api.key", apiKey);
        prop.setProperty("default.from", defaultFrom);
        prop.setProperty("default.sender.name", senderName);

        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            prop.store(output, "Resend Client Configuration");
        } catch (IOException ex) {
            throw new RuntimeException("保存配置失败", ex);
        }
    }

    public String getApiKey() {
        return getProperty("api.key");
    }

    public String getDefaultFrom() {
        return getProperty("default.from");
    }

    public String getSenderName() {
        return getProperty("default.sender.name");
    }

    private String getProperty(String key) {
        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            Properties prop = new Properties();
            prop.load(input);
            return prop.getProperty(key, "");
        } catch (IOException ex) {
            return "";
        }
    }
}