package com.innospots.nexus.core.plugin.installation.domain.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * 可持久化的插件静态摘要，不包含 Factory、Class、Handler、配置值或 Secret。
 *
 * @param pluginId 稳定的插件标识
 * @param version 插件版本
 * @param apiVersion 插件 API 主版本
 * @param sourceType 定义来源类型文本
 * @param sourceLocation 定义来源位置
 * @param capabilities Capability 静态身份摘要列表
 * @param contributions 通用 Contribution 安全摘要列表
 */
public record PluginDefinitionSnapshot(
        String pluginId,
        String version,
        int apiVersion,
        String sourceType,
        String sourceLocation,
        List<CapabilitySnapshot> capabilities,
        List<Map<String, Object>> contributions
) {

    /**
     * Capability 静态身份摘要。
     *
     * @param type Capability 名称
     * @param majorVersion API 主版本
     * @param providerId Provider 标识
     * @param tags 合并后的路由标签
     * @param config 配置 schema 摘要
     */
    public record CapabilitySnapshot(
            String type,
            int majorVersion,
            String providerId,
            Map<String, String> tags,
            List<ConfigItemSnapshot> config
    ) {
        public CapabilitySnapshot {
            tags = tags == null ? Map.of() : Map.copyOf(tags);
            config = config == null ? List.of() : List.copyOf(config);
        }
    }

    /**
     * 配置 schema 的安全摘要。
     *
     * @param key 配置键
     * @param type 配置类型名称
     * @param required 是否必填
     * @param secret 是否敏感
     */
    public record ConfigItemSnapshot(String key, String type, boolean required, boolean secret) {
    }

    public PluginDefinitionSnapshot {
        capabilities = capabilities == null ? List.of() : List.copyOf(capabilities);
        contributions = contributions == null
                ? List.of()
                : contributions.stream().map(PluginDefinitionSnapshot::immutableMap).toList();
    }

    private static Map<String, Object> immutableMap(Map<String, Object> value) {
        if (value == null) {
            return Map.of();
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) freeze(value);
        return result;
    }

    /** 递归冻结快照中的嵌套 Map、List 和 Set，避免可变对象泄漏到安装记录。 */
    private static Object freeze(Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> copy = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!(entry.getKey() instanceof String key)) {
                    throw NexusException.build(
                            PluginStatusCode.PLUGIN_PERSISTENCE_FAILED,
                            "plugin snapshot map keys must be strings");
                }
                copy.put(key, freeze(entry.getValue()));
            }
            return Collections.unmodifiableMap(copy);
        }
        if (value instanceof List<?> list) {
            return list.stream().map(PluginDefinitionSnapshot::freeze).toList();
        }
        if (value instanceof Set<?> set) {
            return Set.copyOf(set.stream().map(PluginDefinitionSnapshot::freeze).toList());
        }
        return value;
    }
}
