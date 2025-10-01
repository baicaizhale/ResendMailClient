package org.resend.mailclient.event;

/**
 * 状态更新事件，当应用程序状态发生变化时触发
 */
public class StatusUpdateEvent {
    private final String message;

    /**
     * 构造函数
     *
     * @param message 状态消息
     */
    public StatusUpdateEvent(String message) {
        this.message = message;
    }

    /**
     * 获取状态消息
     *
     * @return 状态消息
     */
    public String getMessage() {
        return message;
    }
}