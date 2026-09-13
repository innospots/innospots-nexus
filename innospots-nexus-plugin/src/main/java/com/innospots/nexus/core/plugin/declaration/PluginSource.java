package com.innospots.nexus.core.plugin.declaration;

import java.time.Instant;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * 描述插件定义的声明来源和发现时间。
 *
 * @param sourceType 来源类型标识，例如 {@code JAVA} 或 {@code YAML}
 * @param location classpath 类名或资源路径
 * @param discoveredAt 发现时间戳
 */
public record PluginSource(String sourceType, String location, Instant discoveredAt) {

    /**
     * @throws NexusException 来源类型、位置或发现时间为空时抛出
     */
    public PluginSource {
        if (sourceType == null || sourceType.isBlank()
                || location == null || location.isBlank() || discoveredAt == null) {
            throw NexusException.build(
                    PluginStatusCode.PLUGIN_DISCOVERY_FAILED,
                    "plugin source type, location and discovery time are required");
        }
    }

    /**
     * 创建 Java SPI 来源记录。
     *
     * @param location 实现类的完全限定名
     * @param discoveredAt 发现时间戳
     * @return Java SPI 来源记录
     */
    public static PluginSource java(String location, Instant discoveredAt) {
        return new PluginSource("JAVA", location, discoveredAt);
    }

    /**
     * 创建 YAML 资源来源记录。
     *
     * @param location YAML 资源路径
     * @param discoveredAt 发现时间戳
     * @return YAML 资源来源记录
     */
    public static PluginSource yaml(String location, Instant discoveredAt) {
        return new PluginSource("YAML", location, discoveredAt);
    }
}
