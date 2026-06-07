package com.innospots.nexus.base.json;

import java.util.function.Function;

/**
 * Predefined masking strategies for sensitive data during JSON serialization.
 * <p>
 * Each strategy defines how a string value is transformed, keeping a fixed
 * number of leading and/or trailing characters visible and masking the rest
 * with asterisks.
 */
public enum MaskStrategy {

    /** 138****1234 — keep first 3, last 4 */
    PHONE(s -> mask(s, 3, 4)),

    /** a***@example.com — keep first 1 before @, full domain */
    EMAIL(s -> {
        if (s == null || !s.contains("@")) {
            return "***";
        }
        int at = s.indexOf('@');
        String local = s.substring(0, at);
        String domain = s.substring(at);
        return (local.isEmpty() ? "" : local.charAt(0)) + "***" + domain;
    }),

    /** 320***********1234 — keep first 3, last 4 */
    ID_CARD(s -> mask(s, 3, 4)),

    /** 6222****1234 — keep first 4, last 4 */
    BANK_CARD(s -> mask(s, 4, 4)),

    /** 张* / 张三* — keep first char (or first two if length > 2), rest masked */
    NAME(s -> {
        if (s == null || s.isEmpty()) {
            return "***";
        }
        if (s.length() == 2) {
            return s.charAt(0) + "*";
        }
        return s.charAt(0) + s.substring(1, 2) + "*".repeat(Math.max(1, s.length() - 2));
    }),

    /** ****** — fully masked */
    PASSWORD(s -> "******"),

    /** keep no chars, fully replaced with *** */
    HIDE(s -> "***"),

    /** Custom strategy — use {@link MaskValue#keepHead()} and {@link MaskValue#keepTail()} */
    CUSTOM(s -> s);

    private final Function<String, String> masker;

    MaskStrategy(Function<String, String> masker) {
        this.masker = masker;
    }

    /**
     * Apply the masking transformation to the given value.
     *
     * @param value the original string value (may be null)
     * @return the masked string, or null if input is null
     */
    public String apply(String value) {
        if (value == null) {
            return null;
        }
        return masker.apply(value);
    }

    /**
     * Mask a string keeping the first {@code head} and last {@code tail} characters,
     * filling the middle with asterisks.
     */
    static String mask(String s, int head, int tail) {
        if (s == null) {
            return null;
        }
        int len = s.length();
        if (len <= head + tail) {
            return s;
        }
        return s.substring(0, head) + "*".repeat(Math.min(len - head - tail, 4)) + s.substring(len - tail);
    }

    /**
     * Fixed-length masking for custom strategies that don't fit the head/tail pattern.
     */
    static String mask(String s, int head, int tail, int asteriskCount) {
        if (s == null) {
            return null;
        }
        int len = s.length();
        if (len <= head + tail) {
            return s;
        }
        return s.substring(0, head) + "*".repeat(asteriskCount) + s.substring(len - tail);
    }
}
