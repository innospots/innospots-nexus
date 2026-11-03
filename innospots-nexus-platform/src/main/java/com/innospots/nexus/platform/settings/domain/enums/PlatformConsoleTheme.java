package com.innospots.nexus.platform.settings.domain.enums;

import java.util.Locale;
import java.util.Map;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * 运营管理控制台配色主题（与 UI {@code public/themes/<skinKey>.css} 对齐，十二色系）。
 */
@Schema(description = "控制台配色主题")
public enum PlatformConsoleTheme {

    ZHU_CAO("zhu-cao"),
    JIN_YUN("jin-yun"),
    QIU_XIANG("qiu-xiang"),
    BI_SHAN("bi-shan"),
    TIAN_SHUI("tian-shui"),
    QING_MING("qing-ming"),
    NING_YE("ning-ye"),
    LIAN_HONG("lian-hong"),
    LI_KE("li-ke"),
    WAN_YAN("wan-yan"),
    YUE_PO("yue-po"),
    JIN_YU("jin-yu"),

    /** @deprecated 保留存量设置；皮肤映射至 {@link #QING_MING} */
    @Deprecated
    CHENWUNAIFU("qing-ming"),
    /** @deprecated 保留存量设置；皮肤映射至 {@link #BI_SHAN} */
    @Deprecated
    TAIJINGSHENLIN("bi-shan"),
    /** @deprecated 保留存量设置；皮肤映射至 {@link #JIN_YU} */
    @Deprecated
    XUANZHIMOYUN("jin-yu");

    private static final Map<String, String> LEGACY_SKIN_ALIASES = Map.of(
            "chenwunaifu", "qing-ming",
            "taijingshenlin", "bi-shan",
            "xuanzhimoyun", "jin-yu");

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
        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        String resolved = LEGACY_SKIN_ALIASES.getOrDefault(normalized, normalized);
        for (PlatformConsoleTheme theme : values()) {
            if (theme.skinKey.equals(resolved)) {
                return theme;
            }
        }
        return null;
    }
}
