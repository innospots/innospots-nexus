package com.innospots.nexus.core.setting.domain.entity;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

import jakarta.persistence.Column;

import static org.assertj.core.api.Assertions.assertThat;

class SystemSettingEntityContractsTest {

    @Test
    void settingValueUsesStandardStringLength() throws NoSuchFieldException {
        Field field = SystemSettingEntity.class.getDeclaredField("settingValue");
        Column column = field.getAnnotation(Column.class);
        assertThat(column).isNotNull();
        assertThat(column.length()).isEqualTo(SystemSettingEntity.SETTING_VALUE_MAX_LENGTH);
        assertThat(SystemSettingEntity.SETTING_VALUE_MAX_LENGTH).isEqualTo(2048);
    }

    @Test
    void valueTypeColumnLength() throws NoSuchFieldException {
        Field field = SystemSettingEntity.class.getDeclaredField("valueType");
        Column column = field.getAnnotation(Column.class);
        assertThat(column).isNotNull();
        assertThat(column.length()).isEqualTo(32);
    }
}
