package com.innospots.nexus.core.plugin.declaration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.core.plugin.capability.CapabilityKey;
import com.innospots.nexus.core.plugin.capability.CapabilityType;
import com.innospots.nexus.core.plugin.capability.ProviderRef;
import com.innospots.nexus.core.plugin.capability.Tags;
import com.innospots.nexus.core.plugin.config.ConfigDefinition;
import com.innospots.nexus.core.plugin.config.ConfigItemDefinition;
import com.innospots.nexus.core.plugin.contract.CapabilityProvider;
import com.innospots.nexus.core.plugin.contract.CapabilityProviderFactory;
import com.innospots.nexus.core.plugin.contribution.PluginContribution;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * 已通过静态校验的不可变插件运行时定义。
 *
 * <p>定义只保存身份、声明和无副作用工厂，不保存 Provider 实例、运行状态或配置值。</p>
 *
 * @param pluginId 插件反向域名稳定身份
 * @param version 插件发布版本
 * @param apiVersion 插件协议主版本
 * @param displayName 插件本地化显示名称
 * @param description 插件本地化描述
 * @param tags 插件级路由标签
 * @param config 插件共享配置定义
 * @param capabilities 插件提供的 Capability 声明
 * @param requirements 插件依赖的 Capability 声明
 * @param contributions 插件提交给宿主的通用扩展声明
 */
public record PluginDefinition(
        String pluginId,
        String version,
        int apiVersion,
        I18nObject displayName,
        I18nObject description,
        Tags tags,
        ConfigDefinition config,
        List<CapabilityContribution<?>> capabilities,
        List<CapabilityRequirement> requirements,
        List<PluginContribution> contributions
) {

    /** 当前支持的插件协议主版本。 */
    public static final int CURRENT_API_VERSION = 1;

    private static final Pattern PLUGIN_ID_PATTERN = Pattern.compile(
            "[a-z][a-z0-9]*(?:-[a-z0-9]+)*(?:\\.[a-z][a-z0-9]*(?:-[a-z0-9]+)*)+");
    private static final Pattern LANGUAGE_TAG_PATTERN = Pattern.compile(
            "[A-Za-z]{2,8}(?:-[A-Za-z0-9]{1,8})*");

    /**
     * 校验并复制完整插件定义。
     *
     * @throws NexusException 定义字段非法、Provider 标识重复或集合含有空元素
     */
    public PluginDefinition {
        if (pluginId == null || !PLUGIN_ID_PATTERN.matcher(pluginId).matches()) {
            invalid("pluginId must use reverse-domain format: " + pluginId);
        }
        if (version == null || version.isBlank() || version.length() > 64
                || version.chars().anyMatch(Character::isWhitespace)) {
            invalid("plugin version is invalid: " + pluginId);
        }
        if (apiVersion < 1) {
            invalid("plugin apiVersion must be positive: " + pluginId);
        }
        displayName = immutableI18n(displayName, true, "displayName");
        description = immutableI18n(description, false, "description");
        tags = tags == null ? Tags.empty() : tags;
        config = snapshotConfig(config);
        capabilities = immutableCapabilities(capabilities);
        requirements = immutableRequirements(requirements);
        contributions = immutableContributions(contributions);
        requireUniqueProviders(capabilities, pluginId);
        requireUniqueRequirements(requirements);
    }

    /**
     * 保留旧的参数顺序构造形式，避免宿主适配代码在身份迁移期间失去编译能力。
     *
     * @param pluginId 插件反向域名稳定身份
     * @param name 单语言显示名称
     * @param version 插件发布版本
     * @param apiVersion 插件协议主版本
     * @param tags 插件级路由标签
     * @param capabilities Capability 声明
     * @param requirements Capability 依赖
     * @param config 插件配置定义
     */
    public PluginDefinition(
            String pluginId,
            String name,
            String version,
            int apiVersion,
            Tags tags,
            List<CapabilityContribution<?>> capabilities,
            List<CapabilityRequirement> requirements,
            ConfigDefinition config
    ) {
        this(
                pluginId,
                version,
                apiVersion,
                I18nObject.of(name),
                I18nObject.of(Map.of()),
                tags,
                config,
                capabilities,
                requirements,
                List.of());
    }

    /**
     * 返回显示名称的防御性副本，避免可变的 I18nObject 泄漏到运行时定义外部。
     *
     * @return 本地化显示名称副本
     */
    @Override
    public I18nObject displayName() {
        return copyI18n(displayName);
    }

    /**
     * 返回描述的防御性副本。
     *
     * @return 本地化描述副本
     */
    @Override
    public I18nObject description() {
        return copyI18n(description);
    }

    /**
     * 创建声明构建器。
     *
     * @param pluginId 插件反向域名稳定身份
     * @return 可变构建器
     */
    public static Builder builder(String pluginId) {
        return new Builder(pluginId);
    }

    private static I18nObject immutableI18n(I18nObject source, boolean required, String name) {
        if (source == null || (required && source.isEmpty())) {
            invalid("plugin " + name + " is required");
        }
        if (source == null || source.isEmpty()) {
            return I18nObject.of(Map.of());
        }
        if (source.size() > 32) {
            invalid("plugin " + name + " contains too many languages");
        }
        Map<String, String> normalized = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : source.entrySet()) {
            String languageTag = entry.getKey();
            String value = entry.getValue();
            if (languageTag == null || !LANGUAGE_TAG_PATTERN.matcher(languageTag).matches()
                    || value == null || value.isBlank() || value.length() > 256) {
                invalid("plugin " + name + " contains an invalid localized value");
            }
            String canonicalTag = Locale.forLanguageTag(languageTag).toLanguageTag();
            if (canonicalTag.isBlank() || ("und".equals(canonicalTag)
                    && !"und".equalsIgnoreCase(languageTag))) {
                invalid("plugin " + name + " contains an invalid language tag: " + languageTag);
            }
            if (normalized.putIfAbsent(canonicalTag, value) != null) {
                invalid("plugin " + name + " contains duplicate language tag: " + canonicalTag);
            }
        }
        return I18nObject.of(normalized);
    }

    private static I18nObject copyI18n(I18nObject source) {
        return I18nObject.of(new LinkedHashMap<>(source));
    }

    private static List<CapabilityContribution<?>> immutableCapabilities(
            List<CapabilityContribution<?>> source
    ) {
        if (source == null) {
            return List.of();
        }
        for (CapabilityContribution<?> contribution : source) {
            if (contribution == null) {
                invalid("plugin capability contribution must not be null");
            }
        }
        return List.copyOf(source);
    }

    private static List<CapabilityRequirement> immutableRequirements(List<CapabilityRequirement> source) {
        if (source == null) {
            return List.of();
        }
        for (CapabilityRequirement requirement : source) {
            if (requirement == null) {
                invalid("plugin capability requirement must not be null");
            }
        }
        return List.copyOf(source);
    }

    private static List<PluginContribution> immutableContributions(List<PluginContribution> source) {
        if (source == null) {
            return List.of();
        }
        Set<Object> types = new HashSet<>();
        for (PluginContribution contribution : source) {
            if (contribution == null || contribution.type() == null) {
                invalid("plugin contribution must not be null");
            }
            if (!types.add(contribution.type())) {
                invalid("duplicate plugin contribution type: " + contribution.type());
            }
        }
        return List.copyOf(source);
    }

    private static void requireUniqueProviders(
            List<CapabilityContribution<?>> capabilities,
            String pluginId
    ) {
        Set<String> providerIds = new HashSet<>();
        for (CapabilityContribution<?> contribution : capabilities) {
            if (!providerIds.add(contribution.providerId())) {
                throw NexusException.build(
                        PluginStatusCode.PROVIDER_DUPLICATE,
                        "duplicate providerId in plugin " + pluginId + ": " + contribution.providerId());
            }
            // 通过统一的 ProviderRef 再校验一遍组合身份，避免各声明来源产生不同格式。
            new ProviderRef(pluginId, contribution.providerId());
        }
    }

    private static void requireUniqueRequirements(List<CapabilityRequirement> requirements) {
        Set<CapabilityKey> identities = new HashSet<>();
        for (CapabilityRequirement requirement : requirements) {
            if (!identities.add(requirement.key())) {
                invalid("duplicate capability requirement: " + requirement.key());
            }
        }
    }

    private static ConfigDefinition snapshotConfig(ConfigDefinition source) {
        if (source == null) {
            invalid("plugin config definition is required");
        }
        List<ConfigItemDefinition> items;
        try {
            items = List.copyOf(source.items());
        } catch (RuntimeException exception) {
            throw NexusException.build(
                    PluginStatusCode.PLUGIN_DEFINITION_INVALID.fullCode(),
                    "plugin config definition cannot be read",
                    exception);
        }
        Set<String> keys = new HashSet<>();
        for (ConfigItemDefinition item : items) {
            if (item == null || !keys.add(item.key())) {
                invalid("duplicate or null plugin config item");
            }
        }
        return () -> items;
    }

    private static void invalid(String message) {
        throw NexusException.build(PluginStatusCode.PLUGIN_DEFINITION_INVALID, message);
    }

    /**
     * 构建不可变插件定义的链式构建器。
     */
    public static final class Builder {

        private final String pluginId;
        private final List<CapabilityContribution<?>> capabilities = new ArrayList<>();
        private final List<CapabilityRequirement> requirements = new ArrayList<>();
        private final List<PluginContribution> contributions = new ArrayList<>();
        private I18nObject displayName;
        private I18nObject description = I18nObject.of(Map.of());
        private String version;
        private int apiVersion = CURRENT_API_VERSION;
        private Tags tags = Tags.empty();
        private ConfigDefinition config = ConfigDefinition.empty();

        private Builder(String pluginId) {
            this.pluginId = pluginId;
        }

        /**
         * 设置本地化显示名称。
         *
         * @param displayName 插件显示名称
         * @return 当前构建器
         */
        public Builder displayName(I18nObject displayName) {
            this.displayName = displayName;
            return this;
        }

        /**
         * 设置单语言显示名称的便捷形式。
         *
         * @param name 插件显示名称
         * @return 当前构建器
         */
        public Builder name(String name) {
            return displayName(I18nObject.of(name));
        }

        /**
         * 设置本地化描述。
         *
         * @param description 插件描述
         * @return 当前构建器
         */
        public Builder description(I18nObject description) {
            this.description = description;
            return this;
        }

        /**
         * 设置发布版本。
         *
         * @param version 插件发布版本
         * @return 当前构建器
         */
        public Builder version(String version) {
            this.version = version;
            return this;
        }

        /**
         * 设置插件协议主版本。
         *
         * @param apiVersion 插件协议主版本
         * @return 当前构建器
         */
        public Builder apiVersion(int apiVersion) {
            this.apiVersion = apiVersion;
            return this;
        }

        /**
         * 设置由所有 Provider 继承的插件标签。
         *
         * @param tags 插件路由标签
         * @return 当前构建器
         */
        public Builder tags(Tags tags) {
            this.tags = tags;
            return this;
        }

        /**
         * 添加一个使用默认 Provider 标识的 Java 工厂。
         *
         * @param type Capability API 类型
         * @param factory Provider 工厂
         * @param <T> Provider 契约类型
         * @return 当前构建器
         */
        public <T extends CapabilityProvider> Builder provide(
                CapabilityType<T> type,
                CapabilityProviderFactory<? extends T> factory
        ) {
            capabilities.add(new CapabilityContribution<>(type, factory));
            return this;
        }

        /**
         * 添加一个带完整身份、标签和配置的 Provider 工厂。
         *
         * @param type Capability API 类型
         * @param providerId 插件内唯一 Provider 标识
         * @param tags Provider 标签
         * @param config Provider 配置定义
         * @param factory Provider 工厂
         * @param <T> Provider 契约类型
         * @return 当前构建器
         */
        public <T extends CapabilityProvider> Builder provide(
                CapabilityType<T> type,
                String providerId,
                Tags tags,
                ConfigDefinition config,
                CapabilityProviderFactory<? extends T> factory
        ) {
            capabilities.add(new CapabilityContribution<>(type, providerId, tags, config, factory));
            return this;
        }

        /**
         * 添加一个无标签、无 Provider 配置的完整身份工厂。
         *
         * @param type Capability API 类型
         * @param providerId 插件内唯一 Provider 标识
         * @param factory Provider 工厂
         * @param <T> Provider 契约类型
         * @return 当前构建器
         */
        public <T extends CapabilityProvider> Builder provide(
                CapabilityType<T> type,
                String providerId,
                CapabilityProviderFactory<? extends T> factory
        ) {
            return provide(type, providerId, Tags.empty(), ConfigDefinition.empty(), factory);
        }

        /**
         * 添加无标签要求的 Capability 依赖。
         *
         * @param name Capability 逻辑名称
         * @param majorVersion API 主版本
         * @param required 缺少依赖时是否阻止启动
         * @return 当前构建器
         */
        public Builder require(String name, int majorVersion, boolean required) {
            return require(name, majorVersion, Tags.empty(), required);
        }

        /**
         * 添加带标签要求的 Capability 依赖。
         *
         * @param name Capability 逻辑名称
         * @param majorVersion API 主版本
         * @param requiredTags Provider 必须包含的标签
         * @param required 缺少依赖时是否阻止启动
         * @return 当前构建器
         */
        public Builder require(String name, int majorVersion, Tags requiredTags, boolean required) {
            if (name == null || name.isBlank() || majorVersion < 1) {
                invalid("requirement capability name and majorVersion are required");
            }
            requirements.add(new CapabilityRequirement(new CapabilityKey(name, majorVersion), requiredTags, required));
            return this;
        }

        /**
         * 添加无标签要求的 Capability 依赖。
         *
         * @param type 依赖的 Capability 类型
         * @param required 缺少依赖时是否阻止启动
         * @return 当前构建器
         */
        public Builder require(CapabilityType<?> type, boolean required) {
            return require(type, Tags.empty(), required);
        }

        /**
         * 添加带标签要求的 Capability 依赖。
         *
         * @param type 依赖的 Capability 类型
         * @param requiredTags Provider 必须包含的标签
         * @param required 缺少依赖时是否阻止启动
         * @return 当前构建器
         */
        public Builder require(CapabilityType<?> type, Tags requiredTags, boolean required) {
            if (type == null) {
                invalid("requirement capability type is required");
            }
            requirements.add(new CapabilityRequirement(type.key(), requiredTags, required));
            return this;
        }

        /**
         * 设置插件共享配置定义。
         *
         * @param config 插件配置定义
         * @return 当前构建器
         */
        public Builder config(ConfigDefinition config) {
            this.config = config;
            return this;
        }

        /** 添加一份插件级 Contribution 声明。 */
        public Builder contribute(PluginContribution contribution) {
            contributions.add(contribution);
            return this;
        }

        /** 构建并返回不可变定义。 */
        public PluginDefinition build() {
            return new PluginDefinition(
                    pluginId,
                    version,
                    apiVersion,
                    displayName,
                    description,
                    tags,
                    config,
                    capabilities,
                    requirements,
                    contributions);
        }
    }
}
