package com.innospots.nexus.platform.settings.service;

import java.util.Objects;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.core.setting.service.SystemSettingService;
import com.innospots.nexus.platform.settings.domain.enums.PlatformConsoleTheme;
import com.innospots.nexus.platform.settings.domain.request.PlatformConsoleThemeUpdateRequest;
import com.innospots.nexus.platform.settings.domain.vo.PlatformConsoleThemeSettingVo;
import com.innospots.nexus.platform.settings.status.PlatformConsoleUiSettingStatusCode;
import com.innospots.nexus.platform.settings.support.PlatformSettingKeys;
import com.innospots.nexus.platform.support.PlatformMutationLog;

/**
 * 控制台配色主题：{@link PlatformSettingKeys#CONSOLE_THEME}。
 */
@Slf4j
@RequiredArgsConstructor
public class PlatformConsoleThemeSettingService {

    private final SystemSettingService systemSettingService;
    private final PlatformConsoleTheme deploymentDefaultTheme;

    public PlatformConsoleThemeSettingVo getConsoleThemeSetting() {
        PlatformConsoleTheme theme = currentTheme();
        return toVo(theme);
    }

    @Transactional
    public PlatformConsoleThemeSettingVo updateConsoleThemeSetting(PlatformConsoleThemeUpdateRequest request) {
        Objects.requireNonNull(request, "request");
        Checks.notNull(request.theme(), "theme");
        PlatformConsoleTheme updated = systemSettingService.put(
                PlatformSettingKeys.CONSOLE_THEME,
                null,
                request.theme());
        PlatformMutationLog.info(log, "console theme updated theme={}", updated);
        return toVo(updated);
    }

    private PlatformConsoleTheme currentTheme() {
        return systemSettingService.getOrBootstrap(
                PlatformSettingKeys.CONSOLE_THEME,
                null,
                deploymentDefaultTheme);
    }

    private PlatformConsoleThemeSettingVo toVo(PlatformConsoleTheme theme) {
        return new PlatformConsoleThemeSettingVo(
                theme,
                theme.skinKey(),
                systemSettingService.getUpdatedAtOrBootstrap(
                        PlatformSettingKeys.CONSOLE_THEME,
                        null,
                        deploymentDefaultTheme.name()));
    }

    public static PlatformConsoleTheme parseDeploymentDefault(String raw) {
        if (raw == null || raw.isBlank()) {
            return PlatformConsoleTheme.CHENWUNAIFU;
        }
        PlatformConsoleTheme fromSkin = PlatformConsoleTheme.fromSkinKey(raw);
        if (fromSkin != null) {
            return fromSkin;
        }
        try {
            return PlatformConsoleTheme.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw NexusException.build(PlatformConsoleUiSettingStatusCode.CONSOLE_THEME_INVALID);
        }
    }
}
