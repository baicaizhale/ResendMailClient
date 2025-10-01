package org.resend.mailclient.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * 配置服务类，用于管理应用程序配置
 */
public class ConfigService {
    private static final Logger logger = LogManager.getLogger(ConfigService.class);
    private static final String CONFIG_FILE = "mailclient.properties";
    private static final Properties properties = new Properties();
    
    static {
        loadProperties();
    }

    /**
     * 加载配置文件
     */
    private static void loadProperties() {
        Path configPath = Paths.get(CONFIG_FILE);
        
        if (Files.exists(configPath)) {
            try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
                properties.load(fis);
                logger.info("配置文件加载成功: {}", CONFIG_FILE);
            } catch (IOException e) {
                logger.error("加载配置文件失败", e);
            }
        } else {
            logger.info("配置文件不存在，将创建新的配置文件: {}", CONFIG_FILE);
            saveProperties();
        }
    }

    /**
     * 保存配置文件
     */
    private static void saveProperties() {
        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            properties.store(fos, "Resend Mail Client Configuration");
            logger.info("配置文件保存成功: {}", CONFIG_FILE);
        } catch (IOException e) {
            logger.error("保存配置文件失败", e);
        }
    }

    /**
     * 获取配置项
     *
     * @param key 配置键
     * @return 配置值，如果不存在则返回空字符串
     */
    public static String get(String key) {
        return properties.getProperty(key, "");
    }

    /**
     * 获取配置项
     *
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值，如果不存在则返回默认值
     */
    public static String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * 设置配置项
     *
     * @param key 配置键
     * @param value 配置值
     */
    public static void set(String key, String value) {
        properties.setProperty(key, value);
        saveProperties();
    }

    /**
     * 删除配置项
     *
     * @param key 配置键
     */
    public static void remove(String key) {
        properties.remove(key);
        saveProperties();
    }

    /**
     * 清空所有配置
     */
    public static void clear() {
        properties.clear();
        saveProperties();
    }
}