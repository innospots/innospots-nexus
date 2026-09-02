package com.innospots.nexus.core.plugin.contribution;

import java.util.Map;

import com.innospots.nexus.base.exception.NexusException;

/**
 * 将已完成结构校验的通用 YAML 字段解码为具体 Contribution。
 *
 * @param <T> 解码目标类型
 */
public interface PluginContributionDecoder<T extends PluginContribution> {

    /**
     * 返回该 Decoder 支持的类型。
     *
     * @return 贡献类型标识
     */
    PluginContributionType<T> type();

    /**
     * 解码一个不可变声明。
     *
     * @param declaration 已完成结构校验的 YAML 字段映射
     * @return 不可变贡献实例
     * @throws NexusException 字段缺失、类型不匹配或业务规则违反时
     */
    T decode(Map<String, Object> declaration);
}
