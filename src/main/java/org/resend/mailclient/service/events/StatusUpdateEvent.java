package org.resend.mailclient.service.events;

/**
 * 状态更新事件，用于通知UI更新状态信息
 */
public class StatusUpdateEvent {
    private final String message;
    private final boolean isError;

    public StatusUpdateEvent(String message) {
        this(message, false);
    }

    public StatusUpdateEvent(String message, boolean isError) {
        this.message = message;
        this.isError = isError;
    }

    public String getMessage() {
        return message;
    }

    public boolean isError() {
        return isError;
    }
}
