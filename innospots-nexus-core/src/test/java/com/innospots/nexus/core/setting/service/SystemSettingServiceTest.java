package com.innospots.nexus.core.setting.service;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.setting.domain.entity.SystemSettingEntity;
import com.innospots.nexus.core.setting.domain.enums.SettingScope;
import com.innospots.nexus.core.setting.domain.enums.SettingValueType;
import com.innospots.nexus.core.setting.operator.SystemSettingOperator;
import com.innospots.nexus.core.setting.status.SystemSettingStatusCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SystemSettingServiceTest {

    @Test
    void getStringOrBootstrapInsertsWhenMissing() {
        SystemSettingOperator operator = mock(SystemSettingOperator.class);
        when(operator.find("platform", SettingScope.GLOBAL, null, "registration.mode")).thenReturn(null);
        SystemSettingEntity inserted = new SystemSettingEntity();
        inserted.setSettingValue("INVITE");
        when(operator.insert(
                        "platform",
                        SettingScope.GLOBAL,
                        null,
                        "registration.mode",
                        SettingValueType.STRING,
                        "INVITE"))
                .thenReturn(inserted);

        SystemSettingService service = new SystemSettingService(operator);

        assertThat(service.getStringOrBootstrap(
                        "platform", SettingScope.GLOBAL, null, "registration.mode", "INVITE"))
                .isEqualTo("INVITE");
        verify(operator).insert(
                "platform",
                SettingScope.GLOBAL,
                null,
                "registration.mode",
                SettingValueType.STRING,
                "INVITE");
    }

    @Test
    void putStringSkipsUpdateWhenUnchanged() {
        SystemSettingOperator operator = mock(SystemSettingOperator.class);
        SystemSettingEntity entity = new SystemSettingEntity();
        entity.setSettingValue("OPEN");
        entity.setValueType(SettingValueType.STRING.name());
        when(operator.find("platform", SettingScope.GLOBAL, null, "registration.mode")).thenReturn(entity);

        SystemSettingService service = new SystemSettingService(operator);

        assertThat(service.putString("platform", SettingScope.GLOBAL, null, "registration.mode", "OPEN"))
                .isEqualTo("OPEN");
        verify(operator, never()).updateValue(eq(entity), eq("OPEN"));
    }

    @Test
    void putStringRejectsValueLongerThanMaxLength() {
        SystemSettingOperator operator = mock(SystemSettingOperator.class);
        SystemSettingService service = new SystemSettingService(operator);
        String tooLong = "x".repeat(SystemSettingEntity.SETTING_VALUE_MAX_LENGTH + 1);

        assertThatThrownBy(() -> service.putString(
                        "platform", SettingScope.GLOBAL, null, "registration.mode", tooLong))
                .isInstanceOf(NexusException.class)
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(SystemSettingStatusCode.SETTING_VALUE_TOO_LONG.fullCode());
    }
}
