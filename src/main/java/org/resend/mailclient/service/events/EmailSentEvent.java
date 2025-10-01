package org.resend.mailclient.service.events;

import org.resend.mailclient.model.Email;

/**
 * 邮件发送事件，用于通知邮件已发送
 */
public class EmailSentEvent {
    private final Email email;
    private final boolean success;
    private final String errorMessage;

    public EmailSentEvent(Email email, boolean success) {
        this(email, success, null);
    }

    public EmailSentEvent(Email email, boolean success, String errorMessage) {
        this.email = email;
        this.success = success;
        this.errorMessage = errorMessage;
    }

    public Email getEmail() {
        return email;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
