package com.innospots.nexus.core.plugin.config;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * 一个插件的不可变类型化配置快照。
 *
 * <p>{@code values} 保存强类型运行时值；{@code displayValues} 保存日志与诊断用的遮罩文本，
 * 避免 SECRET 明文泄漏到 {@link #toString()}。</p>
 */
final class DefaultPluginConfig implements PluginConfig {

    private final Map<String, Object> values;
    private final Map<String, String> displayValues;

    /**
     * @param values        已解析的配置值快照
     * @param displayValues 与 {@code values} 对齐的遮罩显示文本
     */
    DefaultPluginConfig(Map<String, Object> values, Map<String, String> displayValues) {
        this.values = Map.copyOf(values);
        this.displayValues = Map.copyOf(displayValues);
    }

    @Override
    public Optional<String> get(String key) {
        Object value = values.get(key);
        return value instanceof String text ? Optional.of(text) : Optional.empty();
    }

    @Override
    public String require(String key) {
        return get(key).orElseThrow(() -> invalid("required string config is missing: " + key));
    }

    @Override
    public int getInt(String key, int defaultValue) {
        Object value = values.get(key);
        return value instanceof Integer number ? number : defaultValue;
    }

    @Override
    public long getLong(String key, long defaultValue) {
        Object value = values.get(key);
        return value instanceof Long number ? number : defaultValue;
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        Object value = values.get(key);
        return value instanceof Boolean flag ? flag : defaultValue;
    }

    @Override
    public BigDecimal getDecimal(String key, BigDecimal defaultValue) {
        Object value = values.get(key);
        return value instanceof BigDecimal decimal ? decimal : defaultValue;
    }

    @Override
    public Duration getDuration(String key, Duration defaultValue) {
        Object value = values.get(key);
        return value instanceof Duration duration ? duration : defaultValue;
    }

    @Override
    public URI getUri(String key, URI defaultValue) {
        Object value = values.get(key);
        return value instanceof URI uri ? uri : defaultValue;
    }

    @Override
    public String getEnum(String key, String defaultValue) {
        Object value = values.get(key);
        return value instanceof String text ? text : defaultValue;
    }

    @Override
    public SecretValue requireSecret(String key) {
        Object value = values.get(key);
        if (value instanceof SecretValue secret) {
            // 调用方拥有返回句柄，但不能清除运行时保留的配置快照。
            return secret.copy();
        }
        throw invalid("required secret config is missing: " + key);
    }

    @Override
    public String toString() {
        return new LinkedHashMap<>(displayValues).toString();
    }

    private static NexusException invalid(String message) {
        return NexusException.build(PluginStatusCode.PLUGIN_CONFIG_INVALID, message);
    }
}
