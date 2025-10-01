package org.resend.mailclient.event;

import org.resend.mailclient.model.EmailTemplate;

/**
 * 模板加载事件，当邮件模板被加载时触发
 */
public class TemplateLoadedEvent {
    private final EmailTemplate template;

    /**
     * 构造函数
     *
     * @param template 加载的邮件模板
     */
    public TemplateLoadedEvent(EmailTemplate template) {
        this.template = template;
    }

    /**
     * 获取加载的邮件模板
     *
     * @return 邮件模板对象
     */
    public EmailTemplate getTemplate() {
        return template;
    }
}