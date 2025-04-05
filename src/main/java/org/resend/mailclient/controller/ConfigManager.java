package org.resend.mailclient.service;

import java.io.*;
import java.util.Properties;

public class ConfigManager {
    private static final String CONFIG_PATH = System.getProperty("user.home") + "/.resendmailclient/config.properties";
    private static final Properties prop = new Properties();

    static {
        loadConfig();
    }

    private static void loadConfig() {
        File configFile = new File(CONFIG_PATH);
        if (!configFile.getParentFile().exists()) {
            configFile.getParentFile().mkdirs();
        }

        try (InputStream input = new FileInputStream(configFile)) {
            prop.load(input);
        } catch (IOException e) {
            createDefaultConfig();
        }
    }

    private static void createDefaultConfig() {
        try (OutputStream output = new FileOutputStream(CONFIG_PATH)) {
            prop.setProperty("api.key", "");
            prop.setProperty("sender.name", "MyApp");
            prop.setProperty("sender.email", "no-reply@example.com");
            prop.store(output, "Resend Mail Client Configuration");
        } catch (IOException e) {
            System.err.println("创建配置文件失败: " + e.getMessage());
        }
    }

    public static String get(String key) {
        return prop.getProperty(key);
    }

    public static void save(String key, String value) {
        prop.setProperty(key, value);
        try (OutputStream output = new FileOutputStream(CONFIG_PATH)) {
            prop.store(output, null);
        } catch (IOException e) {
            System.err.println("保存配置失败: " + e.getMessage());
        }
    }

    public static String getConfigPath() {
        return CONFIG_PATH;
    }
}