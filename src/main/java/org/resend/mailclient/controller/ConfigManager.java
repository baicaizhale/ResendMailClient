package org.resend.mailclient.service;

import java.io.*;
import java.util.Properties;

public class ConfigManager {
    private static final File CONFIG_FILE = new File("mailclient.properties");
    private static final Properties prop = new Properties();

    static {
        loadConfig();
    }

    private static void loadConfig() {
        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            prop.load(input);
        } catch (IOException e) {
            createDefaultConfig();
        }
    }

    private static void createDefaultConfig() {
        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            prop.setProperty("api.key", "");
            prop.setProperty("sender.name", "MyApp");
            prop.setProperty("sender.email", "no-reply@example.com");
            prop.store(output, "Mail Client Configuration");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String get(String key) {
        return prop.getProperty(key);
    }

    public static void save(String key, String value) {
        prop.setProperty(key, value);
        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            prop.store(output, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}