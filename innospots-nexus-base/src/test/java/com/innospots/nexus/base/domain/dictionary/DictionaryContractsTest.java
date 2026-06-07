package com.innospots.nexus.base.domain.dictionary;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.i18n.I18nObject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DictionaryContractsTest {

    @Test
    void definesDictionaryTypeAndItem() {
        DictionaryType type = new DictionaryType("language", I18nObject.of("Language"), BasicStatus.ENABLED);
        DictionaryItem item = new DictionaryItem("zh-CN", I18nObject.of("Chinese"), "language", I18nObject.of("Language"), BasicStatus.ENABLED);

        assertThat(type.code()).isEqualTo("language");
        assertThat(item.type()).isEqualTo(type.code());
        assertThat(item.name().defaultValue()).isEqualTo("Chinese");
    }
}
