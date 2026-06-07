package com.innospots.nexus.base.i18n;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class I18nObjectTest {

    @AfterEach
    void clearContext() {
        I18nConverter.clear();
    }

    @Test
    void createsI18nObjectFromDefaultLocalePairsAndMap() {
        I18nObject object = I18nObject.of("zh-CN", "名称", "en", "Name");

        assertThat(object.value(Locale.SIMPLIFIED_CHINESE)).isEqualTo("名称");
        assertThat(object.value(Locale.US)).isEqualTo("Name");
        assertThat(I18nObject.of(Map.of("en", "Name")).value(Locale.UK)).isEqualTo("Name");
        assertThat(I18nObject.of("Default").value(Locale.US)).isEqualTo("Default");
    }

    @Test
    void rejectsOddLocaleValuePairs() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> I18nObject.of("en", "Name", "zh-CN"));
    }

    @Test
    void fallsBackToFirstValueWhenLocaleIsMissing() {
        I18nObject object = I18nObject.of("ja", "名前");

        assertThat(object.value(Locale.US)).isEqualTo("名前");
    }
}
