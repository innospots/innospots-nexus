package com.innospots.nexus.platform.settings.service;

import java.util.Objects;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.core.setting.service.SystemSettingService;
import com.innospots.nexus.platform.settings.domain.enums.PlatformNavigationLayoutMode;
import com.innospots.nexus.platform.settings.domain.request.PlatformNavigationLayoutUpdateRequest;
import com.innospots.nexus.platform.settings.domain.vo.PlatformNavigationLayoutSettingVo;
import com.innospots.nexus.platform.settings.status.PlatformConsoleUiSettingStatusCode;
import com.innospots.nexus.platform.settings.support.PlatformSettingKeys;
import com.innospots.nexus.console.support.MutationLog;

/**
 * 控制台导航布局：{@link PlatformSettingKeys#NAVIGATION_LAYOUT}。
 */
@Slf4j
@RequiredArgsConstructor
public class PlatformNavigationLayoutSettingService {

    private final SystemSettingService systemSettingService;
    private final PlatformNavigationLayoutMode deploymentDefaultMode;

    public PlatformNavigationLayoutSettingVo getNavigationLayoutSetting() {
        return toVo(currentMode());
    }

    @Transactional
    public PlatformNavigationLayoutSettingVo updateNavigationLayoutSetting(
            PlatformNavigationLayoutUpdateRequest request) {
        Objects.requireNonNull(request, "request");
        Checks.notNull(request.navigationLayoutMode(), "navigationLayoutMode");
        PlatformNavigationLayoutMode updated = systemSettingService.put(
                PlatformSettingKeys.NAVIGATION_LAYOUT,
                null,
                request.navigationLayoutMode());
        MutationLog.info(log, "navigation layout updated mode={}", updated);
        return toVo(updated);
    }

    private PlatformNavigationLayoutMode currentMode() {
        return systemSettingService.getOrBootstrap(
                PlatformSettingKeys.NAVIGATION_LAYOUT,
                null,
                deploymentDefaultMode);
    }

    private PlatformNavigationLayoutSettingVo toVo(PlatformNavigationLayoutMode mode) {
        return new PlatformNavigationLayoutSettingVo(
                mode,
                systemSettingService.getUpdatedAtOrBootstrap(
                        PlatformSettingKeys.NAVIGATION_LAYOUT,
                        null,
                        deploymentDefaultMode.name()));
    }

    public static PlatformNavigationLayoutMode parseDeploymentDefault(String raw) {
        if (raw == null || raw.isBlank()) {
            return PlatformNavigationLayoutMode.RAIL_DUAL;
        }
        try {
            return PlatformNavigationLayoutMode.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw NexusException.build(PlatformConsoleUiSettingStatusCode.NAVIGATION_LAYOUT_INVALID);
        }
    }
}
