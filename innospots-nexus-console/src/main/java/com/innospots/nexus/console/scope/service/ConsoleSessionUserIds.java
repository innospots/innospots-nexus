package com.innospots.nexus.console.scope.service;

/**
 * 将会话中的主体用户标识规范为 {@link com.innospots.nexus.base.thread.TLC#userId()} 可用的 Long。
 * 平台/租户用户主键为 opaque 字符串（如 {@code PUS…}）时，与 {@link com.innospots.nexus.console.jaxrs.support.ConsoleTokenSessionBinder} 一致使用稳定哈希。
 */
public final class ConsoleSessionUserIds {

    private ConsoleSessionUserIds() {
    }

    /**
     * @param subjectUserId 平台或租户用户主键
     * @return 数值型 TLC 用户 ID；空白输入返回 null
     */
    public static Long resolveNumericUserId(String subjectUserId) {
        if (subjectUserId == null || subjectUserId.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(subjectUserId);
        } catch (NumberFormatException ex) {
            return (long) subjectUserId.hashCode();
        }
    }
}
