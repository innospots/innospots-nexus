package com.innospots.nexus.spring.core.i18n;

import com.innospots.nexus.base.i18n.I18nConverter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class MessageSourceI18nMessageResolverTest {

    @AfterEach
    void clearContext() {
        I18nConverter.clear();
    }

    private static ResourceBundleMessageSource testMessageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasenames("i18n/messages");
        messageSource.setDefaultEncoding("UTF-8");
        // 与 {@link NexusI18nConfiguration} 一致，避免 JVM 默认 zh_CN 时回退到系统语言
        messageSource.setFallbackToSystemLocale(false);
        return messageSource;
    }

    @Test
    void resolvesMessageFromResourceBundle() {
        MessageSourceI18nMessageResolver resolver =
                new MessageSourceI18nMessageResolver(testMessageSource());

        assertThat(resolver.resolve("app.title", Locale.US)).isEqualTo("Nexus");
        assertThat(resolver.resolve("app.title", Locale.SIMPLIFIED_CHINESE)).isEqualTo("智汇");
        assertThat(resolver.resolve("missing.key", Locale.US)).isNull();
    }

    @Test
    void integratesWithI18nConverter() {
        I18nConverter.setMessageResolver(new MessageSourceI18nMessageResolver(testMessageSource()));
        I18nConverter.setLocale(Locale.US);

        assertThat(I18nConverter.translate("${app.title}")).isEqualTo("Nexus");
    }
}
