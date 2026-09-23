package com.innospots.nexus.spring.core.i18n;

import com.innospots.nexus.base.i18n.I18nMessageResolver;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;

import java.util.Locale;
import java.util.Objects;

/**
 * 基于 Spring {@link MessageSource} 与 ResourceBundle 的 {@link I18nMessageResolver} 实现。
 *
 * @author Smars
 * @date 2026/09/16
 * @see I18nMessageResolver
 */
public class MessageSourceI18nMessageResolver implements I18nMessageResolver {

    private final MessageSource messageSource;

    /**
     * @param messageSource Spring 消息源（通常来自 {@code spring.messages.basename} 配置）
     */
    public MessageSourceI18nMessageResolver(MessageSource messageSource) {
        this.messageSource = Objects.requireNonNull(messageSource, "messageSource");
    }

    @Override
    public String resolve(String key, Locale locale) {
        if (key == null || key.isBlank()) {
            return null;
        }
        Locale effectiveLocale = locale == null ? Locale.getDefault() : locale;
        try {
            return messageSource.getMessage(key, null, effectiveLocale);
        } catch (NoSuchMessageException ex) {
            return null;
        }
    }
}
