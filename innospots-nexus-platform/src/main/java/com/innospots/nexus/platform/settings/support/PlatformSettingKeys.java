package com.innospots.nexus.platform.settings.support;

import com.innospots.nexus.core.setting.domain.enums.SettingScope;
import com.innospots.nexus.core.setting.domain.enums.SettingValueType;
import com.innospots.nexus.core.setting.support.SettingKey;
import com.innospots.nexus.platform.settings.domain.enums.PlatformRegistrationMode;

/**
 * 平台模块在 {@link com.innospots.nexus.core.setting.service.SystemSettingService} 中的设置域与键。
 *
 * @author Smars
 * @date 2026/10/06
 */
public final class PlatformSettingKeys {

    /** 平台设置域标识。 */
    public static final String DOMAIN = "platform";

    /** 自助注册模式：{@code registration.mode}。 */
    public static final SettingKey<PlatformRegistrationMode> REGISTRATION_MODE = new SettingKey<>(
            DOMAIN,
            "registration.mode",
            SettingScope.GLOBAL,
            SettingValueType.STRING,
            raw -> PlatformRegistrationMode.valueOf(raw.trim().toUpperCase()));

    private PlatformSettingKeys() {
    }
}
