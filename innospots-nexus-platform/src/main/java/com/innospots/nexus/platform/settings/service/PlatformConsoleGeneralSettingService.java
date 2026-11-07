package com.innospots.nexus.platform.settings.service;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.Objects;
import java.util.Set;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.core.setting.service.SystemSettingService;
import com.innospots.nexus.platform.settings.domain.request.PlatformConsoleDisplayLanguageUpdateRequest;
import com.innospots.nexus.platform.settings.domain.request.PlatformConsoleSiteDescriptionUpdateRequest;
import com.innospots.nexus.platform.settings.domain.request.PlatformConsoleSiteNameUpdateRequest;
import com.innospots.nexus.platform.settings.domain.request.PlatformConsoleTimezoneUpdateRequest;
import com.innospots.nexus.platform.settings.domain.vo.PlatformConsoleDisplayLanguageSettingVo;
import com.innospots.nexus.platform.settings.domain.vo.PlatformConsoleGeneralSettingsVo;
import com.innospots.nexus.platform.settings.domain.vo.PlatformConsoleSiteDescriptionSettingVo;
import com.innospots.nexus.platform.settings.domain.vo.PlatformConsoleSiteNameSettingVo;
import com.innospots.nexus.platform.settings.domain.vo.PlatformConsoleTimezoneSettingVo;
import com.innospots.nexus.platform.settings.status.PlatformConsoleUiSettingStatusCode;
import com.innospots.nexus.platform.settings.support.PlatformSettingKeys;
import com.innospots.nexus.console.support.MutationLog;

/**
 * 控制台基础系统设置：名称、简介、界面语言与默认时区。
 */
@Slf4j
@RequiredArgsConstructor
public class PlatformConsoleGeneralSettingService {

    private static final int SITE_NAME_MAX_LENGTH = 80;
    private static final int SITE_DESCRIPTION_MAX_LENGTH = 200;

    private static final Set<String> ALLOWED_LANGUAGES = Set.of("zh-CN", "en-US");

    private static final String DEFAULT_SITE_NAME = "Nexus Console";
    private static final String DEFAULT_SITE_DESCRIPTION = "运营管理平台";
    private static final String DEFAULT_LANGUAGE = "zh-CN";
    private static final String DEFAULT_TIMEZONE = "Asia/Shanghai";

    private final SystemSettingService systemSettingService;
    private final String deploymentSiteName;
    private final String deploymentSiteDescription;
    private final String deploymentDisplayLanguage;
    private final String deploymentTimezone;

    public PlatformConsoleGeneralSettingsVo getConsoleGeneralSettings() {
        return new PlatformConsoleGeneralSettingsVo(
                currentSiteName(),
                currentSiteDescription(),
                currentDisplayLanguage(),
                currentTimezone());
    }

    public PlatformConsoleSiteNameSettingVo getSiteNameSetting() {
        return toSiteNameVo(currentSiteName());
    }

    @Transactional
    public PlatformConsoleSiteNameSettingVo updateSiteNameSetting(PlatformConsoleSiteNameUpdateRequest request) {
        Objects.requireNonNull(request, "request");
        String normalized = normalizeSiteName(request.siteName());
        String updated = systemSettingService.put(
                PlatformSettingKeys.CONSOLE_SITE_NAME,
                null,
                normalized);
        MutationLog.info(log, "console site name updated");
        return toSiteNameVo(updated);
    }

    public PlatformConsoleSiteDescriptionSettingVo getSiteDescriptionSetting() {
        return toSiteDescriptionVo(currentSiteDescription());
    }

    @Transactional
    public PlatformConsoleSiteDescriptionSettingVo updateSiteDescriptionSetting(
            PlatformConsoleSiteDescriptionUpdateRequest request) {
        Objects.requireNonNull(request, "request");
        String normalized = normalizeSiteDescription(request.description());
        String updated = systemSettingService.put(
                PlatformSettingKeys.CONSOLE_SITE_DESCRIPTION,
                null,
                normalized);
        MutationLog.info(log, "console site description updated");
        return toSiteDescriptionVo(updated);
    }

    public PlatformConsoleDisplayLanguageSettingVo getDisplayLanguageSetting() {
        return toDisplayLanguageVo(currentDisplayLanguage());
    }

    @Transactional
    public PlatformConsoleDisplayLanguageSettingVo updateDisplayLanguageSetting(
            PlatformConsoleDisplayLanguageUpdateRequest request) {
        Objects.requireNonNull(request, "request");
        Checks.notNull(request.language(), "language");
        String normalized = normalizeDisplayLanguage(request.language());
        String updated = systemSettingService.put(
                PlatformSettingKeys.CONSOLE_DISPLAY_LANGUAGE,
                null,
                normalized);
        MutationLog.info(log, "console display language updated language={}", updated);
        return toDisplayLanguageVo(updated);
    }

    public PlatformConsoleTimezoneSettingVo getTimezoneSetting() {
        return toTimezoneVo(currentTimezone());
    }

    @Transactional
    public PlatformConsoleTimezoneSettingVo updateTimezoneSetting(PlatformConsoleTimezoneUpdateRequest request) {
        Objects.requireNonNull(request, "request");
        Checks.notNull(request.timezone(), "timezone");
        String normalized = normalizeTimezone(request.timezone());
        String updated = systemSettingService.put(
                PlatformSettingKeys.CONSOLE_TIMEZONE,
                null,
                normalized);
        MutationLog.info(log, "console timezone updated timezone={}", updated);
        return toTimezoneVo(updated);
    }

    private String currentSiteName() {
        return systemSettingService.getOrBootstrap(
                PlatformSettingKeys.CONSOLE_SITE_NAME,
                null,
                deploymentSiteName);
    }

    private String currentSiteDescription() {
        return systemSettingService.getOrBootstrap(
                PlatformSettingKeys.CONSOLE_SITE_DESCRIPTION,
                null,
                deploymentSiteDescription);
    }

    private String currentDisplayLanguage() {
        return systemSettingService.getOrBootstrap(
                PlatformSettingKeys.CONSOLE_DISPLAY_LANGUAGE,
                null,
                deploymentDisplayLanguage);
    }

    private String currentTimezone() {
        return systemSettingService.getOrBootstrap(
                PlatformSettingKeys.CONSOLE_TIMEZONE,
                null,
                deploymentTimezone);
    }

    private PlatformConsoleSiteNameSettingVo toSiteNameVo(String siteName) {
        return new PlatformConsoleSiteNameSettingVo(
                siteName,
                systemSettingService.getUpdatedAtOrBootstrap(
                        PlatformSettingKeys.CONSOLE_SITE_NAME,
                        null,
                        deploymentSiteName));
    }

    private PlatformConsoleSiteDescriptionSettingVo toSiteDescriptionVo(String description) {
        return new PlatformConsoleSiteDescriptionSettingVo(
                description,
                systemSettingService.getUpdatedAtOrBootstrap(
                        PlatformSettingKeys.CONSOLE_SITE_DESCRIPTION,
                        null,
                        deploymentSiteDescription));
    }

    private PlatformConsoleDisplayLanguageSettingVo toDisplayLanguageVo(String language) {
        return new PlatformConsoleDisplayLanguageSettingVo(
                language,
                systemSettingService.getUpdatedAtOrBootstrap(
                        PlatformSettingKeys.CONSOLE_DISPLAY_LANGUAGE,
                        null,
                        deploymentDisplayLanguage));
    }

    private PlatformConsoleTimezoneSettingVo toTimezoneVo(String timezone) {
        return new PlatformConsoleTimezoneSettingVo(
                timezone,
                systemSettingService.getUpdatedAtOrBootstrap(
                        PlatformSettingKeys.CONSOLE_TIMEZONE,
                        null,
                        deploymentTimezone));
    }

    private static String normalizeSiteName(String raw) {
        if (raw == null) {
            throw NexusException.build(PlatformConsoleUiSettingStatusCode.CONSOLE_SITE_NAME_INVALID);
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty() || trimmed.length() > SITE_NAME_MAX_LENGTH) {
            throw NexusException.build(PlatformConsoleUiSettingStatusCode.CONSOLE_SITE_NAME_INVALID);
        }
        return trimmed;
    }

    private static String normalizeSiteDescription(String raw) {
        if (raw == null) {
            return "";
        }
        String trimmed = raw.trim();
        if (trimmed.length() > SITE_DESCRIPTION_MAX_LENGTH) {
            throw NexusException.build(PlatformConsoleUiSettingStatusCode.CONSOLE_SITE_DESCRIPTION_INVALID);
        }
        return trimmed;
    }

    private static String normalizeDisplayLanguage(String raw) {
        String trimmed = raw.trim();
        if (!ALLOWED_LANGUAGES.contains(trimmed)) {
            throw NexusException.build(PlatformConsoleUiSettingStatusCode.CONSOLE_DISPLAY_LANGUAGE_INVALID);
        }
        return trimmed;
    }

    private static String normalizeTimezone(String raw) {
        if (raw == null) {
            throw NexusException.build(PlatformConsoleUiSettingStatusCode.CONSOLE_TIMEZONE_INVALID);
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            throw NexusException.build(PlatformConsoleUiSettingStatusCode.CONSOLE_TIMEZONE_INVALID);
        }
        try {
            return ZoneId.of(trimmed).getId();
        } catch (DateTimeException ex) {
            throw NexusException.build(PlatformConsoleUiSettingStatusCode.CONSOLE_TIMEZONE_INVALID);
        }
    }

    public static String parseDeploymentSiteName(String raw) {
        if (raw == null || raw.isBlank()) {
            return DEFAULT_SITE_NAME;
        }
        return normalizeSiteName(raw);
    }

    public static String parseDeploymentSiteDescription(String raw) {
        if (raw == null) {
            return DEFAULT_SITE_DESCRIPTION;
        }
        if (raw.isBlank()) {
            return "";
        }
        return normalizeSiteDescription(raw);
    }

    public static String parseDeploymentDisplayLanguage(String raw) {
        if (raw == null || raw.isBlank()) {
            return DEFAULT_LANGUAGE;
        }
        return normalizeDisplayLanguage(raw);
    }

    public static String parseDeploymentTimezone(String raw) {
        if (raw == null || raw.isBlank()) {
            return DEFAULT_TIMEZONE;
        }
        return normalizeTimezone(raw);
    }
}
