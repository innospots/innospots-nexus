package com.innospots.nexus.platform.settings.service;

import java.util.Objects;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.core.setting.service.SystemSettingService;
import com.innospots.nexus.platform.settings.domain.enums.PlatformRegistrationMode;
import com.innospots.nexus.platform.settings.domain.request.PlatformRegistrationPolicyUpdateRequest;
import com.innospots.nexus.platform.settings.domain.vo.PlatformRegistrationPolicyVo;
import com.innospots.nexus.platform.settings.status.PlatformRegistrationModeSettingStatusCode;
import com.innospots.nexus.platform.settings.support.PlatformSettingKeys;

/**
 * 平台自助注册模式设置：基于 {@link SystemSettingService} 的 {@code platform.registration.mode}。
 *
 * @author Smars
 * @date 2026/10/06
 * @see com.innospots.nexus.platform.settings.endpoint.PlatformRegistrationModeSettingEndpoint
 */
@RequiredArgsConstructor
public class PlatformRegistrationModeSettingService {

    private final SystemSettingService systemSettingService;
    private final PlatformRegistrationMode deploymentDefaultMode;

    public PlatformRegistrationPolicyVo getRegistrationModeSetting() {
        PlatformRegistrationMode mode = currentMode();
        return new PlatformRegistrationPolicyVo(
                mode,
                systemSettingService.getUpdatedAtOrBootstrap(
                        PlatformSettingKeys.REGISTRATION_MODE,
                        null,
                        deploymentDefaultMode.name()));
    }

    @Transactional
    public PlatformRegistrationPolicyVo updateRegistrationModeSetting(PlatformRegistrationPolicyUpdateRequest request) {
        Objects.requireNonNull(request, "request");
        Checks.notNull(request.registrationMode(), "registrationMode");
        PlatformRegistrationMode updated = systemSettingService.put(
                PlatformSettingKeys.REGISTRATION_MODE,
                null,
                request.registrationMode());
        return new PlatformRegistrationPolicyVo(
                updated,
                systemSettingService.getUpdatedAtOrBootstrap(
                        PlatformSettingKeys.REGISTRATION_MODE,
                        null,
                        deploymentDefaultMode.name()));
    }

    /**
     * 自助注册流程门禁：当前模式须与期望一致。
     *
     * @param expected 流程对应的模式
     */
    public void requireSelfServiceMode(PlatformRegistrationMode expected) {
        PlatformRegistrationMode current = currentMode();
        if (current != expected) {
            throw NexusException.build(PlatformRegistrationModeSettingStatusCode.REGISTRATION_MODE_DISABLED);
        }
    }

    private PlatformRegistrationMode currentMode() {
        return systemSettingService.getOrBootstrap(
                PlatformSettingKeys.REGISTRATION_MODE,
                null,
                deploymentDefaultMode);
    }

    public static PlatformRegistrationMode parseDeploymentDefault(String raw) {
        if (raw == null || raw.isBlank()) {
            return PlatformRegistrationMode.INVITE;
        }
        try {
            return PlatformRegistrationMode.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw NexusException.build(PlatformRegistrationModeSettingStatusCode.REGISTRATION_MODE_INVALID);
        }
    }
}
