package com.innospots.nexus.core.plugin.declaration;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * YAML DSL 的纯数据模型。
 *
 * <p>该模型不保存 Java Class、Factory、Provider、ClassLoader 或运行时配置值。</p>
 */
public record PluginManifest(
        String apiVersion,
        String kind,
        Metadata metadata,
        Spec spec
) {

    /** 插件稳定身份和发布版本。 */
    public record Metadata(String pluginId, String version) {
    }

    /** 插件协议、名称、配置和扩展声明。 */
    public record Spec(
            Integer apiVersion,
            Map<String, String> displayName,
            Map<String, String> description,
            Map<String, String> tags,
            List<ConfigItem> config,
            List<Requirement> requirements,
            List<Capability> capabilities,
            List<Map<String, Object>> contributions
    ) {

        /** 对缺省可选字段提供空集合，避免下游出现可变空值。 */
        public Spec {
            description = description == null ? Map.of() : Map.copyOf(description);
            tags = tags == null ? Map.of() : Map.copyOf(tags);
            config = config == null ? List.of() : List.copyOf(config);
            requirements = requirements == null ? List.of() : List.copyOf(requirements);
            capabilities = capabilities == null ? List.of() : List.copyOf(capabilities);
            contributions = contributions == null ? List.of() : List.copyOf(contributions);
        }
    }

    /** 一个插件或 Provider 的配置项。 */
    public record ConfigItem(
            String key,
            String type,
            Boolean required,
            @JsonProperty("default")
            Object defaultValue,
            String description,
            List<String> enumValues
    ) {

        /** 对缺省枚举和值描述提供稳定默认值。 */
        public ConfigItem {
            enumValues = enumValues == null ? List.of() : List.copyOf(enumValues);
        }

        /** 返回适用于当前配置模型的文本默认值。 */
        public String defaultText() {
            return defaultValue == null ? null : String.valueOf(defaultValue);
        }
    }

    /** 一个 Capability 依赖声明。 */
    public record Requirement(
            String type,
            @JsonProperty("majorVersion")
            Integer majorVersion,
            Boolean required,
            Map<String, String> tags
    ) {

        /** 规范化缺省依赖字段。 */
        public Requirement {
            tags = tags == null ? Map.of() : Map.copyOf(tags);
        }
    }

    /** 一个 Capability Provider 的显式 Java 绑定声明。 */
    public record Capability(
            String type,
            Integer majorVersion,
            String providerId,
            String api,
            Map<String, String> tags,
            List<ConfigItem> config,
            Bind bind,
            List<Map<String, Object>> exposures
    ) {

        /** 规范化缺省 Provider 字段。 */
        public Capability {
            tags = tags == null ? Map.of() : Map.copyOf(tags);
            config = config == null ? List.of() : List.copyOf(config);
            exposures = exposures == null ? List.of() : List.copyOf(exposures);
        }
    }

    /** Capability 的绑定方式；V1 仅实现 kind=java。 */
    public record Bind(String kind, @JsonProperty("class") String className) {
    }
}
