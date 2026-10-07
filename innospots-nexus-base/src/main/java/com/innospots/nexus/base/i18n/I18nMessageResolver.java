package com.innospots.nexus.base.i18n;

import java.util.Locale;

/**
 * 将 i18n 键解析为本地化消息字符串的策略接口。
 * 实现可从资源包、数据库或任何其他存储加载消息。
 *
 * @author Smars
 * @date 2026/09/13
 * @see I18nConverter
 */
@FunctionalInterface
public interface I18nMessageResolver {

    /**
     * 解析指定键在给定语言环境下的消息。
     *
     * @param key    i18n 键
     * @param locale 语言环境
     * @return 本地化消息；未找到时返回 null
     */
    String resolve(String key, Locale locale);
}
