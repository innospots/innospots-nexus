package com.innospots.nexus.base.json;

import java.util.function.Function;

/**
 * JSON 序列化时敏感数据的预定义脱敏策略。
 * <p>
 * 每种策略定义字符串值的转换方式：保留固定数量的前导和/或尾部字符，
 * 其余部分用星号遮蔽。
 *
 * @author Smars
 * @date 2026/09/13
 * @see MaskValue
 * @see MaskedSerializer
 */
public enum MaskStrategy {

    /** 138****1234 — 保留前 3 位、后 4 位 */
    PHONE(s -> mask(s, 3, 4)),

    /** a***@example.com — 保留 @ 前第 1 位，域名完整保留 */
    EMAIL(s -> {
        if (s == null || !s.contains("@")) {
            return "***";
        }
        int at = s.indexOf('@');
        String local = s.substring(0, at);
        String domain = s.substring(at);
        return (local.isEmpty() ? "" : local.charAt(0)) + "***" + domain;
    }),

    /** 320***********1234 — 保留前 3 位、后 4 位 */
    ID_CARD(s -> mask(s, 3, 4)),

    /** 6222****1234 — 保留前 4 位、后 4 位 */
    BANK_CARD(s -> mask(s, 4, 4)),

    /** 张* / 张三* — 保留首字（长度 > 2 时保留前两个），其余遮蔽 */
    NAME(s -> {
        if (s == null || s.isEmpty()) {
            return "***";
        }
        if (s.length() == 2) {
            return s.charAt(0) + "*";
        }
        return s.charAt(0) + s.substring(1, 2) + "*".repeat(Math.max(1, s.length() - 2));
    }),

    /** ****** — 完全遮蔽 */
    PASSWORD(s -> "******"),

    /** 不保留任何字符，全部替换为 *** */
    HIDE(s -> "***"),

    /** 自定义策略 — 使用 {@link MaskValue#keepHead()} 和 {@link MaskValue#keepTail()} */
    CUSTOM(s -> s);

    private final Function<String, String> masker;

    MaskStrategy(Function<String, String> masker) {
        this.masker = masker;
    }

    /**
     * 对给定值应用脱敏转换。
     *
     * @param value 原始字符串值（可为 null）
     * @return 脱敏后的字符串；输入为 null 时返回 null
     */
    public String apply(String value) {
        if (value == null) {
            return null;
        }
        return masker.apply(value);
    }

    /**
     * 遮蔽字符串，保留前 {@code head} 位和后 {@code tail} 位，中间用星号填充。
     *
     * @param s    原始字符串
     * @param head 前导保留字符数
     * @param tail 尾部保留字符数
     * @return 脱敏后的字符串
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
     * 固定长度遮蔽，用于不适合前导/尾部模式的自定义策略。
     *
     * @param s             原始字符串
     * @param head          前导保留字符数
     * @param tail          尾部保留字符数
     * @param asteriskCount 星号数量
     * @return 脱敏后的字符串
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
