package com.innospots.nexus.console.logger;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.i18n.I18nConverter;
import com.innospots.nexus.console.logger.domain.context.InvocationLogContext;

class LogExecutorTest {

    @AfterEach
    void tearDown() {
        I18nConverter.setLocale(null);
    }

    @Test
    void executePersistsResolvedOperationMessage() throws Throwable {
        I18nConverter.setLocale(Locale.US);
        AtomicReference<InvocationLogContext> captured = new AtomicReference<>();
        LogExecutor executor = new LogExecutor(captured::set);

        Object result = executor.execute(
                EndpointGrantFixtures.dictionaryTypeCreateGrant(),
                "com.example.DictionaryEndpoint",
                "createType",
                "alice",
                new Object[] {"arg"},
                () -> "ok");

        assertThat(result).isEqualTo("ok");
        InvocationLogContext context = captured.get();
        assertThat(context).isNotNull();
        assertThat(context.operationMessage()).isEqualTo("Create dictionary type");
        assertThat(context.action()).isEqualTo("dictionary.type.create");
        assertThat(context.success()).isTrue();
    }
}
