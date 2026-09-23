package com.innospots.nexus.service.observability.logging;

import java.util.Locale;
import java.util.regex.Pattern;

import com.innospots.nexus.base.util.Checks;

/**
 * 敏感值掩码器。
 *
 * @author Smars
 * @date 2026/09/15
 */
public final class SensitiveValueMasker {

    private static final String MASK = "***";
    private static final Pattern JWT_PATTERN = Pattern.compile("^[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+$");
    private static final Pattern API_KEY_PATTERN = Pattern.compile("^(sk|pk|api)[-_][A-Za-z0-9_-]{8,}$", Pattern.CASE_INSENSITIVE);

    /**
     * 掩码头值。
     *
     * @param headerName  头名
     * @param headerValue 头值
     * @return 掩码后的值
     */
    public String maskHeader(String headerName, String headerValue) {
        Checks.notBlank(headerName, "headerName");
        if (headerValue == null || headerValue.isBlank()) {
            return "";
        }
        String normalized = headerName.toLowerCase(Locale.ROOT);
        if ("authorization".equals(normalized) || "cookie".equals(normalized) || "set-cookie".equals(normalized)) {
            return MASK;
        }
        return mask(headerValue);
    }

    /**
     * 掩码通用值。
     *
     * @param value 原始值
     * @return 掩码后的值
     */
    public String mask(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return "Bearer " + MASK;
        }
        if (JWT_PATTERN.matcher(trimmed).matches()) {
            return MASK;
        }
        if (API_KEY_PATTERN.matcher(trimmed).matches()) {
            return MASK;
        }
        return trimmed;
    }
}
