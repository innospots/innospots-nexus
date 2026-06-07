package com.innospots.nexus.base.i18n;

import com.innospots.nexus.base.json.Jsons;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class I18nConverterTest {

    @AfterEach
    void clearContext() {
        I18nConverter.clear();
    }

    @Test
    void translatesMarkedStringsWithResolverAndDefaultValues() {
        I18nConverter.setLocale(Locale.US);
        I18nConverter.setMessageResolver((key, locale) -> "app.title".equals(key) ? "Nexus" : null);

        assertThat(I18nConverter.translate("${app.title}")).isEqualTo("Nexus");
        assertThat(I18nConverter.translate("${missing:中文|#|English}")).isEqualTo("English");

        I18nConverter.setLocale(Locale.SIMPLIFIED_CHINESE);

        assertThat(I18nConverter.translate("${missing:中文|#|English}")).isEqualTo("中文");
    }

    @Test
    void translatesNestedMapsListsAndI18nObjects() {
        I18nConverter.setLocale(Locale.US);
        I18nConverter.setMessageResolver((key, locale) -> "Enabled");

        Object translated = I18nConverter.translate(Map.of(
                "label", I18nObject.of("zh-CN", "名称", "en", "Name"),
                "status", "${status.enabled}",
                "items", List.of("${status.enabled}", Map.of("name", I18nObject.of("en", "Item")))
        ));

        assertThat(translated).isInstanceOf(Map.class);
        Map<?, ?> map = (Map<?, ?>) translated;
        assertThat(map.get("label")).isEqualTo("Name");
        assertThat(map.get("status")).isEqualTo("Enabled");
        List<?> items = (List<?>) map.get("items");
        assertThat(items.get(0)).isEqualTo("Enabled");
        assertThat(items.get(1)).isEqualTo(Map.of("name", "Item"));
    }

    @Test
    void ignoreModePreservesOriginalI18nPayload() {
        I18nConverter.ignoreI18n();

        Object translated = I18nConverter.translate(I18nObject.of("en", "Name"));

        assertThat(translated).isInstanceOf(I18nObject.class);
        assertThat((I18nObject) translated).containsEntry("en", "Name");
    }

    @Test
    void serializesAnnotatedFieldsWithI18nConversion() {
        I18nConverter.setLocale(Locale.US);
        I18nConverter.setMessageResolver((key, locale) -> "app.title".equals(key) ? "Nexus" : null);

        String json = Jsons.toJson(new DemoPayload(
                "${app.title}",
                I18nObject.of("zh-CN", "名称", "en", "Name"),
                List.of("${missing:中文|#|English}")
        ));

        assertThat(json).contains("\"title\":\"Nexus\"");
        assertThat(json).contains("\"name\":\"Name\"");
        assertThat(json).contains("\"items\":[\"English\"]");
    }

    record DemoPayload(@I18n String title, @I18n I18nObject name, @I18n List<String> items) {
    }
}
