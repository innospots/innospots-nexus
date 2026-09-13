package com.innospots.nexus.core.plugin.declaration;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * YAML DSL 的纯数据模型。
 *
 * <p>该模型不保存 Java Class、Factory、Provider、ClassLoader 或运行时配置值。</p>
 *
 * @param apiVersion 文档头协议版本，例如 {@code nexus.plugin/v1}
 * @param kind       文档类型，例如 {@code Plugin}
 * @param metadata   插件稳定身份与发布版本
 * @param spec       插件协议、名称、配置和扩展声明
 */
public record PluginManifest(
        String apiVersion,
        String kind,
        Metadata metadata,
        Spec spec
) {

    /**
     * 插件稳定身份和发布版本。
     *
     * @param pluginId 反向域名插件标识
     * @param version  发布版本文本
     */
    public record Metadata(String pluginId, String version) {
    }

    /**
     * 插件协议、名称、配置和扩展声明。
     *
     * @param apiVersion    插件协议主版本
     * @param displayName   本地化显示名称
     * @param description   本地化描述；可为空映射
     * @param tags          插件级路由标签
     * @param config        插件共享配置项
     * @param requirements  Capability 依赖
     * @param capabilities  Capability Provider 声明
     * @param contributions 通用 Contribution 声明
     */
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

    /**
     * 一个插件或 Provider 的配置项。
     *
     * @param key         配置键
     * @param type        配置类型名称，对应 {@link com.innospots.nexus.core.plugin.config.ConfigType}
     * @param required    是否必填
     * @param defaultValue 默认值；SECRET 类型不得设置
     * @param description 人类可读说明
     * @param enumValues  ENUM 类型的合法取值
     */
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

    /**
     * 一个 Capability 依赖声明。
     *
     * @param type         Capability 逻辑名称
     * @param majorVersion API 主版本
     * @param required     缺少依赖时是否阻止启动；缺省为 {@code true}
     * @param tags         Provider 必须包含的路由标签
     */
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

    /**
     * 一个 Capability Provider 的显式 Java 绑定声明。
     *
     * @param type         Capability 逻辑名称
     * @param majorVersion API 主版本
     * @param providerId   插件内唯一 Provider 标识
     * @param api          Provider 实现的 Java API 接口全名
     * @param tags         Provider 专属路由标签
     * @param config       Provider 专属配置项
     * @param bind         绑定方式；V1 仅支持 {@code kind=java}
     * @param exposures    预留的对外暴露声明；V1 不支持
     */
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

    /**
     * Capability 的绑定方式；V1 仅实现 {@code kind=java}。
     *
     * @param kind      绑定种类
     * @param className Java 实现类的完全限定名
     */
    public record Bind(String kind, @JsonProperty("class") String className) {
    }
}
