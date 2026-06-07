package com.innospots.nexus.base.i18n;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class I18nAnnotationTest {

    @AfterEach
    void clearContext() {
        I18nConverter.clear();
    }

    record I18nPayload(
            @I18n String title,
            @I18n I18nObject name,
            @I18n List<String> items,
            String untouched
    ) {
    }

    record NullablePayload(@I18n String value) {
    }

    private static final ObjectMapper MAPPER = new JsonMapper();

    @Nested
    class SerializationTests {

        @Test
        void translatesI18nKeyString() throws Exception {
            I18nConverter.setLocale(Locale.US);
            I18nConverter.setMessageResolver((key, locale) -> "app.title".equals(key) ? "Dashboard" : null);

            String json = MAPPER.writeValueAsString(new I18nPayload(
                    "${app.title}", I18nObject.of("en", "Name"), List.of(), "plain"
            ));

            assertThat(json).contains("\"title\":\"Dashboard\"");
        }

        @Test
        void translatesI18nObject() throws Exception {
            I18nConverter.setLocale(Locale.SIMPLIFIED_CHINESE);
            I18nConverter.setMessageResolver((key, locale) -> null);

            String json = MAPPER.writeValueAsString(new I18nPayload(
                    "plain", I18nObject.of("zh-CN", "名称", "en", "Name"), List.of(), "x"
            ));

            assertThat(json).contains("\"name\":\"名称\"");
        }

        @Test
        void switchesLocale() throws Exception {
            I18nConverter.setLocale(Locale.US);

            String jsonEn = MAPPER.writeValueAsString(new I18nPayload(
                    "static", I18nObject.of("zh-CN", "中文", "en", "English"), List.of(), "x"
            ));

            assertThat(jsonEn).contains("\"name\":\"English\"");

            I18nConverter.setLocale(Locale.SIMPLIFIED_CHINESE);

            String jsonZh = MAPPER.writeValueAsString(new I18nPayload(
                    "static", I18nObject.of("zh-CN", "中文", "en", "English"), List.of(), "x"
            ));

            assertThat(jsonZh).contains("\"name\":\"中文\"");
        }

        @Test
        void fallsBackToInlineDefault() throws Exception {
            I18nConverter.setLocale(Locale.US);
            I18nConverter.setMessageResolver((key, locale) -> null);

            String json = MAPPER.writeValueAsString(new I18nPayload(
                    "${missing.key:Fallback}", I18nObject.of("en", "Name"), List.of(), "x"
            ));

            assertThat(json).contains("\"title\":\"Fallback\"");
        }

        @Test
        void nullFieldSerializesAsNull() throws Exception {
            I18nConverter.setLocale(Locale.US);
            I18nConverter.setMessageResolver((key, locale) -> "v");

            String json = MAPPER.writeValueAsString(new NullablePayload(null));

            assertThat(json).contains("\"value\":null");
        }
    }

    @Nested
    class FieldSerializerDirectTests {

        @Test
        void serializerDelegatesToConverter() throws Exception {
            I18nConverter.setLocale(Locale.US);
            I18nConverter.setMessageResolver((key, locale) -> key.equals("key") ? "resolved" : null);

            String json = MAPPER.writeValueAsString(new I18nPayload("${key}", null, null, "x"));

            assertThat(json).contains("\"title\":\"resolved\"");
        }

        @Test
        void serializerPassesThroughPlainStrings() throws Exception {
            I18nConverter.setLocale(Locale.US);
            I18nConverter.setMessageResolver((key, locale) -> "SHOULD_NOT_BE_USED");

            String json = MAPPER.writeValueAsString(new I18nPayload("hello", null, null, "x"));

            assertThat(json).contains("\"title\":\"hello\"");
        }
    }
}
