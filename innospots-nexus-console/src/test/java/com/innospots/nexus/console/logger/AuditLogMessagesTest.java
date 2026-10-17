package com.innospots.nexus.console.logger;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.i18n.I18nConverter;
import com.innospots.nexus.base.i18n.I18nObject;

class AuditLogMessagesTest {

    @AfterEach
    void tearDown() {
        I18nConverter.setLocale(null);
    }

    @Test
    void resolvesOperationMessageForCurrentLocale() {
        I18nConverter.setLocale(Locale.SIMPLIFIED_CHINESE);
        AuditLog auditLog = AuditLogAnnotationFixtures.dictionaryTypeCreate();

        assertThat(AuditLogMessages.resolveOperationMessage(auditLog)).isEqualTo("创建字典类型");
    }

    @Test
    void resolvesOperationMessageForEnglishLocale() {
        I18nConverter.setLocale(Locale.US);
        AuditLog auditLog = AuditLogAnnotationFixtures.dictionaryTypeCreate();

        assertThat(AuditLogMessages.resolveOperationMessage(auditLog)).isEqualTo("Create dictionary type");
    }

    @Test
    void convertsLiteralPairsToI18nObject() {
        I18nObject object = AuditLogMessages.toI18nObject(AuditLogAnnotationFixtures.dictionaryTypeCreate().value());

        assertThat(object.cnValue()).isEqualTo("创建字典类型");
        assertThat(object.enValue()).isEqualTo("Create dictionary type");
    }

}
