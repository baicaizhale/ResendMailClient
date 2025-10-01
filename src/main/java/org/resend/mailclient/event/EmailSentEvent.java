package org.resend.mailclient.event;

import org.resend.mailclient.model.Email;

/**
 * 邮件发送事件，当邮件发送成功或失败时触发
 */
public class EmailSentEvent {
    private final Email email;
    private final boolean success;

    /**
     * 构造函数
     *
     * @param email 发送的邮件
     * @param success 是否发送成功
     */
    public EmailSentEvent(Email email, boolean success) {
        this.email = email;
        this.success = success;
    }

    /**
     * 获取发送的邮件
     *
     * @return 邮件对象
     */
    public Email getEmail() {
        return email;
    }

    /**
     * 判断邮件是否发送成功
     *
     * @return 是否成功
     */
    public boolean isSuccess() {
        return success;
    }
}