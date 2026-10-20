package com.innospots.nexus.console.logger.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.i18n.I18nConverter;
import com.innospots.nexus.console.logger.EndpointGrantFixtures;
import com.innospots.nexus.console.logger.domain.context.InvocationLogContext;

class AuditLogRecorderTest {

    @AfterEach
    void tearDown() {
        I18nConverter.setLocale(null);
    }

    @Test
    void recordsHttpPathAndLocalizedMessage() {
        I18nConverter.setLocale(Locale.SIMPLIFIED_CHINESE);
        AtomicReference<InvocationLogContext> captured = new AtomicReference<>();
        AuditLogRecorder recorder = new AuditLogRecorder(captured::set);
        AuditLogInvocationState state = new AuditLogInvocationState(
                EndpointGrantFixtures.dictionaryTypeCreateGrant(),
                "com.example.DictionaryEndpoint",
                "createType",
                "POST",
                "/platform/dictionary-types",
                System.currentTimeMillis() - 5);

        recorder.record(state, "alice", 200, null, null);

        InvocationLogContext context = captured.get();
        assertThat(context).isNotNull();
        assertThat(context.httpPath()).isEqualTo("POST /platform/dictionary-types");
        assertThat(context.operationMessage()).isEqualTo("创建字典类型");
        assertThat(context.actor()).isEqualTo("alice");
        assertThat(context.success()).isTrue();
    }
}
