package com.swp.pizzashop.messages;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

/**
 * Central helper to fetch i18n messages from messages.properties (and other bundles if added).
 * Supports both enum-based codes (SystemMessageCode) and arbitrary message keys.
 */
@Service
public class MessageService {
    private final MessageSource messageSource;

    public MessageService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * Resolve a message by enum code. Falls back to the code itself if not found.
     * @param code SystemMessageCode enum
     * @param args Optional format arguments ({0}, {1}, ...)
     */
    public String get(SystemMessageCode code, Object... args) {
        return messageSource.getMessage(code.code(), args, code.code(), LocaleContextHolder.getLocale());
    }

    /**
     * Resolve a message by plain key.
     * @param key message key
     * @param args Optional format arguments ({0}, {1}, ...)
     */
    public String raw(String key, Object... args) {
        return messageSource.getMessage(key, args, key, LocaleContextHolder.getLocale());
    }

    /**
     * Convenience for field-specific messages where the first placeholder is a field name.
     */
    public String field(SystemMessageCode code, String fieldDisplayName) {
        return get(code, fieldDisplayName);
    }
}

