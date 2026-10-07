package com.innospots.nexus.base.json;

import com.innospots.nexus.base.i18n.I18n;
import com.innospots.nexus.base.i18n.I18nConverter;
import com.innospots.nexus.base.i18n.I18nObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class I18nObjectDeserializerTest {

    record Payload(I18nObject label) {
    }

    record I18nStringPayload(@I18n String title) {
    }

    @AfterEach
    void clearContext() {
        I18nConverter.clear();
    }

    @Test
    void deserializesStringAsDefaultLocale() {
        Payload payload = Jsons.fromJson("{\"label\":\"Name\"}", Payload.class);

        assertThat(payload.label()).containsEntry("en", "Name");
    }

    @Test
    void deserializesLocaleMap() {
        Payload payload = Jsons.fromJson(
                "{\"label\":{\"zh-CN\":\"名称\",\"en\":\"Name\"}}",
                Payload.class
        );

        assertThat(payload.label().value(Locale.US)).isEqualTo("Name");
        assertThat(payload.label().value(Locale.SIMPLIFIED_CHINESE)).isEqualTo("名称");
    }

    @Test
    void deserializesAnnotatedStringFromLocaleObject() {
        I18nConverter.setLocale(Locale.US);

        I18nStringPayload payload = Jsons.fromJson(
                "{\"title\":{\"zh-CN\":\"名称\",\"en\":\"Name\"}}",
                I18nStringPayload.class
        );

        assertThat(payload.title()).isEqualTo("Name");
    }

    @Test
    void roundTripsLocalizedWireFormat() {
        I18nConverter.setLocale(Locale.US);
        Payload original = new Payload(I18nObject.of("zh-CN", "名称", "en", "Name"));

        String json = Jsons.toJson(original);
        Payload restored = Jsons.fromJson(json, Payload.class);

        assertThat(json).isEqualTo("{\"label\":\"Name\"}");
        assertThat(restored.label().value(Locale.US)).isEqualTo("Name");
    }

    @Test
    void preservesFullStructureWhenIgnoreI18n() {
        I18nConverter.ignoreI18n();
        Payload original = new Payload(I18nObject.of("en", "Name"));

        String json = Jsons.toJson(original);
        Payload restored = Jsons.fromJson(json, Payload.class);

        assertThat(restored.label()).containsExactlyEntriesOf(Map.of("en", "Name"));
    }
}
