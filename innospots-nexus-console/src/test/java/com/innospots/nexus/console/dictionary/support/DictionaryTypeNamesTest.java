package com.innospots.nexus.console.dictionary.support;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.console.dictionary.domain.entity.DictionaryTypeEntity;

import static org.assertj.core.api.Assertions.assertThat;

class DictionaryTypeNamesTest {

    @Test
    void applyPersistsJsonAndZhCnSummary() {
        DictionaryTypeEntity entity = new DictionaryTypeEntity();
        DictionaryTypeNames.apply(
                entity,
                I18nObject.of("zh-CN", "用户状态", "en", "User status"));

        assertThat(entity.getTypeName()).isEqualTo("用户状态");
        assertThat(entity.getTypeNameI18n()).contains("\"en\":\"User status\"");
        assertThat(DictionaryTypeNames.read(entity).get("en")).isEqualTo("User status");
    }

    @Test
    void mergeKeepsExistingLocales() {
        I18nObject merged = DictionaryTypeNames.merge(
                I18nObject.of("zh-CN", "用户状态", "en", "User status"),
                I18nObject.of("en", "User status updated"));

        assertThat(merged.get("zh-CN")).isEqualTo("用户状态");
        assertThat(merged.get("en")).isEqualTo("User status updated");
    }

    @Test
    void readFallsBackToLegacySingleColumn() {
        DictionaryTypeEntity entity = new DictionaryTypeEntity();
        entity.setTypeName("历史名称");

        I18nObject names = DictionaryTypeNames.read(entity);

        assertThat(names.get(DictionaryTypeNames.LOCALE_ZH_CN)).isEqualTo("历史名称");
        assertThat(DictionaryTypeNames.displayLabel(entity)).isEqualTo("历史名称");
    }
}
