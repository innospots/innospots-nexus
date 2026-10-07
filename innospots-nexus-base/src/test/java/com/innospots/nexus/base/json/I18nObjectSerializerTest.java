package com.innospots.nexus.base.json;

import com.innospots.nexus.base.i18n.I18nConverter;
import com.innospots.nexus.base.i18n.I18nObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class I18nObjectSerializerTest {

    record Payload(I18nObject label) {
    }

    @AfterEach
    void clearContext() {
        I18nConverter.clear();
    }

    @Test
    void serializesLocalizedStringWithoutI18nFieldAnnotation() throws Exception {
        I18nConverter.setLocale(Locale.US);

        String json = Jsons.toJson(new Payload(
                I18nObject.of("zh-CN", "中文", "en", "English")));

        assertThat(json).isEqualTo("{\"label\":\"English\"}");
    }

    @Test
    void serializesFullStructureWhenIgnoreI18n() throws Exception {
        I18nConverter.setLocale(Locale.US);
        I18nConverter.ignoreI18n();

        String json = Jsons.toJson(new Payload(
                I18nObject.of("zh-CN", "中文", "en", "English")));

        assertThat(json).contains("\"zh-CN\":\"中文\"");
        assertThat(json).contains("\"en\":\"English\"");
    }

    @Test
    void serializesNullI18nObjectAsNull() throws Exception {
        I18nConverter.setLocale(Locale.US);

        String json = Jsons.toJson(new Payload(null));

        assertThat(json).isEqualTo("{\"label\":null}");
    }
}
