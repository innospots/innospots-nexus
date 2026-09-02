package com.innospots.nexus.core.plugin.config;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;
import java.util.Optional;

import com.innospots.nexus.base.exception.NexusException;

/**
 * 限定在一个插件命名空间内的不可变已校验配置视图。
 */
public interface PluginConfig {

    /**
     * 查找字符串配置值。
     *
     * @param key 插件本地配置键
     * @return 已解析的字符串值；键不存在时为空
     */
    Optional<String> get(String key);

    /**
     * 返回必填字符串配置值。
     *
     * @param key 插件本地配置键
     * @return 已解析的字符串值
     * @throws NexusException 键不存在或值非法时
     */
    String require(String key);

    /**
     * 返回整数配置值。
     *
     * @param key          插件本地配置键
     * @param defaultValue 键不存在时的回退值
     * @return 已解析的整数或回退值
     */
    int getInt(String key, int defaultValue);

    /**
     * 返回长整数配置值。
     *
     * @param key          插件本地配置键
     * @param defaultValue 键不存在时的回退值
     * @return 已解析的长整数或回退值
     */
    long getLong(String key, long defaultValue);

    /**
     * 返回布尔配置值。
     *
     * @param key          插件本地配置键
     * @param defaultValue 键不存在时的回退值
     * @return 已解析的布尔值或回退值
     */
    boolean getBoolean(String key, boolean defaultValue);

    /**
     * 返回十进制定点数配置值。
     *
     * <p>默认实现不读取配置，直接返回 {@code defaultValue}；具体实现应覆盖此方法。
     *
     * @param key          插件本地配置键
     * @param defaultValue 键不存在时的回退值
     * @return 已解析的十进制值或回退值
     */
    default BigDecimal getDecimal(String key, BigDecimal defaultValue) {
        return defaultValue;
    }

    /**
     * 返回时长配置值。
     *
     * @param key          插件本地配置键
     * @param defaultValue 键不存在时的回退值
     * @return 已解析的时长或回退值
     */
    Duration getDuration(String key, Duration defaultValue);

    /**
     * 返回绝对 URI 配置值。
     *
     * <p>默认实现不读取配置，直接返回 {@code defaultValue}；具体实现应覆盖此方法。
     *
     * @param key          插件本地配置键
     * @param defaultValue 键不存在时的回退值
     * @return 已解析的 URI 或回退值
     */
    default URI getUri(String key, URI defaultValue) {
        return defaultValue;
    }

    /**
     * 返回枚举配置值。
     *
     * <p>默认实现不读取配置，直接返回 {@code defaultValue}；具体实现应覆盖此方法。
     *
     * @param key          插件本地配置键
     * @param defaultValue 键不存在时的回退值
     * @return 已解析的枚举文本或回退值
     */
    default String getEnum(String key, String defaultValue) {
        return defaultValue;
    }

    /**
     * 返回必填且通过遮罩访问的密文配置值。
     *
     * @param key 插件本地配置键
     * @return 可关闭的密文包装器，调用方负责关闭
     * @throws NexusException 键不存在、值为空或类型不匹配时
     */
    SecretValue requireSecret(String key);
}
