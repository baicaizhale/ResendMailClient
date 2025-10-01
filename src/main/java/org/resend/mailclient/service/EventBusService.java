package org.resend.mailclient.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 事件总线服务，用于在应用程序中传递事件
 */
public class EventBusService {
    private static final Logger logger = LogManager.getLogger(EventBusService.class);
    private static final Map<Class<?>, List<Consumer<?>>> subscribers = new HashMap<>();

    /**
     * 注册事件订阅者
     *
     * @param eventType 事件类型
     * @param subscriber 事件订阅者
     * @param <T> 事件类型
     */
    public static <T> void subscribe(Class<T> eventType, Consumer<T> subscriber) {
        subscribers.computeIfAbsent(eventType, k -> new ArrayList<>()).add(subscriber);
        logger.debug("已注册事件订阅者: {}", eventType.getSimpleName());
    }

    /**
     * 取消注册事件订阅者
     *
     * @param eventType 事件类型
     * @param subscriber 事件订阅者
     * @param <T> 事件类型
     */
    public static <T> void unsubscribe(Class<T> eventType, Consumer<T> subscriber) {
        if (subscribers.containsKey(eventType)) {
            subscribers.get(eventType).remove(subscriber);
            logger.debug("已取消注册事件订阅者: {}", eventType.getSimpleName());
        }
    }

    /**
     * 发布事件
     *
     * @param event 事件对象
     * @param <T> 事件类型
     */
    @SuppressWarnings("unchecked")
    public static <T> void post(T event) {
        Class<?> eventType = event.getClass();
        logger.debug("发布事件: {}", eventType.getSimpleName());
        
        if (subscribers.containsKey(eventType)) {
            for (Consumer<?> subscriber : subscribers.get(eventType)) {
                try {
                    ((Consumer<T>) subscriber).accept(event);
                } catch (Exception e) {
                    logger.error("事件处理异常", e);
                }
            }
        }
    }

    /**
     * 清除所有事件订阅者
     */
    public static void clearAllSubscribers() {
        subscribers.clear();
        logger.debug("已清除所有事件订阅者");
    }
}