package com.innospots.nexus.platform.settings.domain.enums;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * 运营管理控制台配色主题（与 UI {@code public/skins/<skinKey>.css} 对齐）。
 */
@Schema(description = "控制台配色主题")
public enum PlatformConsoleTheme {

    CHENWUNAIFU("chenwunaifu"),
    TAIJINGSHENLIN("taijingshenlin"),
    XUANZHIMOYUN("xuanzhimoyun");

    private final String skinKey;

    PlatformConsoleTheme(String skinKey) {
        this.skinKey = skinKey;
    }

    public String skinKey() {
        return skinKey;
    }

    public static PlatformConsoleTheme fromSkinKey(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String normalized = raw.trim().toLowerCase();
        for (PlatformConsoleTheme theme : values()) {
            if (theme.skinKey.equals(normalized)) {
                return theme;
            }
        }
        return null;
    }
}
