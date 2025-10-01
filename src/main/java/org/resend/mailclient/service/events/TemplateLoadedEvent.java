package org.resend.mailclient.service.events;

import org.resend.mailclient.model.EmailTemplate;

/**
 * 模板加载事件，用于通知模板已加载
 */
public class TemplateLoadedEvent {
    private final EmailTemplate template;

    public TemplateLoadedEvent(EmailTemplate template) {
        this.template = template;
    }

    public EmailTemplate getTemplate() {
        return template;
    }
}
