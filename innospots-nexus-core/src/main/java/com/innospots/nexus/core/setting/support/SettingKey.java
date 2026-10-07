package com.innospots.nexus.core.setting.support;

import java.util.function.Function;

import com.innospots.nexus.core.setting.domain.enums.SettingScope;
import com.innospots.nexus.core.setting.domain.enums.SettingValueType;

/**
 * 类型化设置项标识：业务模块在编译期声明域、键、范围、值类型与字符串解析器。
 *
 * @param domain    设置域，如 {@code platform}、{@code console}
 * @param key       域内键，如 {@code registration.mode}
 * @param scope     作用范围
 * @param valueType 持久化 {@code value_type} 列
 * @param parser    将库内字符串解析为业务类型；失败时应抛异常以便映射为校验错误
 * @param <T>       解析后的业务类型
 * @author Smars
 * @date 2026/10/06
 * @see com.innospots.nexus.core.setting.service.SystemSettingService#getOrBootstrap
 * @see com.innospots.nexus.core.setting.service.SystemSettingService#put
 */
public record SettingKey<T>(
        String domain,
        String key,
        SettingScope scope,
        SettingValueType valueType,
        Function<String, T> parser
) {

    /**
     * 构造 {@link SettingValueType#STRING} 设置键（解析为原文字符串）。
     *
     * @param domain 设置域
     * @param key    设置键
     * @param scope  作用范围
     * @return 字符串设置键
     */
    public static SettingKey<String> ofString(String domain, String key, SettingScope scope) {
        return new SettingKey<>(domain, key, scope, SettingValueType.STRING, Function.identity());
    }
}
