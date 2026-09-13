package com.innospots.nexus.core.plugin.config;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.capability.ProviderRef;
import com.innospots.nexus.core.plugin.declaration.CapabilityContribution;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * 解析插件默认值、宿主配置、动态配置来源、环境变量、系统属性和运行时覆盖值。
 */
public final class ConfigurationManager {

    private final Map<String, String> hostConfig;
    private final List<ConfigSource> configSources;
    private final Map<String, String> environment;
    private final Map<String, String> systemProperties;
    private final Map<String, String> runtimeVariables;

    /**
     * 创建一个按宿主到运行时优先级排列配置来源的解析器。
     *
     * @param hostConfig 宿主提供的扁平化配置
     * @param configSources 宿主动态配置来源；按列表顺序叠加
     * @param environment 进程环境变量
     * @param systemProperties JVM 系统属性
     * @param runtimeVariables 优先级最高的运行时覆盖值
     */
    public ConfigurationManager(
            Map<String, String> hostConfig,
            List<ConfigSource> configSources,
            Map<String, String> environment,
            Map<String, String> systemProperties,
            Map<String, String> runtimeVariables
    ) {
        this.hostConfig = immutable(hostConfig);
        this.configSources = immutableSources(configSources);
        this.environment = immutable(environment);
        this.systemProperties = immutable(systemProperties);
        this.runtimeVariables = immutable(runtimeVariables);
    }

    /**
     * 兼容构造：无动态配置来源。
     */
    public ConfigurationManager(
            Map<String, String> hostConfig,
            Map<String, String> environment,
            Map<String, String> systemProperties,
            Map<String, String> runtimeVariables
    ) {
        this(hostConfig, List.of(), environment, systemProperties, runtimeVariables);
    }

    /**
     * 创建一个使用当前进程环境变量和系统属性的解析器。
     *
     * @param hostConfig 宿主提供的扁平化配置
     * @param runtimeVariables 优先级最高的运行时覆盖值
     * @return 使用当前进程配置来源的解析器
     */
    public static ConfigurationManager standard(
            Map<String, String> hostConfig,
            Map<String, String> runtimeVariables
    ) {
        return standard(hostConfig, List.of(), runtimeVariables);
    }

    /**
     * 创建一个使用当前进程环境变量、系统属性和宿主动态配置来源的解析器。
     *
     * @param hostConfig 宿主提供的扁平化配置
     * @param configSources 宿主动态配置来源
     * @param runtimeVariables 优先级最高的运行时覆盖值
     * @return 使用当前进程配置来源的解析器
     */
    public static ConfigurationManager standard(
            Map<String, String> hostConfig,
            List<ConfigSource> configSources,
            Map<String, String> runtimeVariables
    ) {
        Map<String, String> properties = System.getProperties().entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(
                        entry -> String.valueOf(entry.getKey()),
                        entry -> String.valueOf(entry.getValue())));
        return new ConfigurationManager(hostConfig, configSources, System.getenv(), properties, runtimeVariables);
    }

    /**
     * 解析并校验一个插件隔离的配置快照。
     *
     * @param definition 待解析 schema 的不可变插件声明
     * @return 限定在插件命名空间内的类型化配置
     * @throws NexusException 出现未知键、缺少必填值或转换错误时抛出
     */
    public PluginConfig resolve(PluginDefinition definition) {
        if (definition == null) {
            throw invalid("plugin definition is required for configuration resolution");
        }
        return resolveScope(
                definition.pluginId(),
                "",
                definition.config().items(),
                "plugin");
    }

    /**
     * 解析某个 Provider 的私有配置。私有配置和插件共享配置使用不同命名空间，避免相互覆盖。
     *
     * @param definition 插件定义
     * @param contribution Provider 声明
     * @param <T> Provider 类型
     * @return Provider 私有配置视图
     */
    public <T extends com.innospots.nexus.core.plugin.contract.CapabilityProvider> PluginConfig resolveProvider(
            PluginDefinition definition,
            CapabilityContribution<T> contribution
    ) {
        if (definition == null || contribution == null) {
            throw invalid("plugin definition and capability contribution are required");
        }
        new ProviderRef(definition.pluginId(), contribution.providerId());
        String providerPath = ".providers." + contribution.providerId();
        return resolveScope(
                definition.pluginId(),
                providerPath,
                contribution.config().items(),
                "provider " + contribution.providerId());
    }

    /**
     * 将一个完整插件配置键映射为确定性的环境变量名。
     *
     * @param pluginId 稳定的插件标识
     * @param itemKey 插件本地配置键
     * @return 确定性的环境变量名
     * @throws NexusException 任一键为空时抛出
     */
    public static String environmentName(String pluginId, String itemKey) {
        if (pluginId == null || pluginId.isBlank() || itemKey == null || itemKey.isBlank()) {
            throw invalid("plugin id and config key are required for environment mapping");
        }
        return "NEXUS_PLUGIN_" + (pluginId + "." + itemKey)
                .replace('-', '_')
                .replace('.', '_')
                .toUpperCase(java.util.Locale.ROOT);
    }

    /**
     * 校验全部已发现插件定义的环境变量映射。
     *
     * @param definitions 作为一个 classpath 快照校验的插件声明
     * @throws NexusException 两个逻辑键映射到同一环境变量名时抛出
     */
    public static void validateEnvironmentNames(Collection<PluginDefinition> definitions) {
        if (definitions == null) {
            throw invalid("plugin definitions are required for environment validation");
        }
        Map<String, String> names = new HashMap<>();
        for (PluginDefinition definition : definitions) {
            if (definition == null) {
                throw invalid("plugin definition must not be null");
            }
            for (ConfigItemDefinition item : definition.config().items()) {
                registerEnvironmentName(names, definition.pluginId() + "." + item.key(),
                        environmentName(definition.pluginId(), item.key()));
            }
            for (CapabilityContribution<?> contribution : definition.capabilities()) {
                for (ConfigItemDefinition item : contribution.config().items()) {
                    String fullKey = definition.pluginId() + ".providers."
                            + contribution.providerId() + "." + item.key();
                    registerEnvironmentName(names, fullKey,
                            environmentName(definition.pluginId()
                                    + ".providers." + contribution.providerId(), item.key()));
                }
            }
        }
    }

    private PluginConfig resolveScope(
            String pluginId,
            String nestedPath,
            Collection<ConfigItemDefinition> definitions,
            String scopeName
    ) {
        String prefix = "plugins." + pluginId + nestedPath + ".";
        Map<String, ConfigItemDefinition> items = definitions.stream()
                .collect(Collectors.toUnmodifiableMap(ConfigItemDefinition::key, item -> item));
        rejectUnknown(prefix, items.keySet(), hostConfig);
        rejectUnknown(prefix, items.keySet(), systemProperties);
        rejectUnknown(prefix, items.keySet(), runtimeVariables);
        // 每个 ConfigSource 在单次解析中只调用一次 values()，避免动态来源产生不一致快照。
        List<Map<String, String>> dynamicValues = configSources.stream()
                .map(ConfigSource::values)
                .toList();
        for (Map<String, String> values : dynamicValues) {
            rejectUnknown(prefix, items.keySet(), values);
        }

        Map<String, String> merged = new LinkedHashMap<>();
        for (ConfigItemDefinition item : definitions) {
            if (item.defaultValue() != null) {
                merged.put(item.key(), item.defaultValue());
            }
        }
        // 合并优先级：default → hostConfig → configSources → env → system props → runtimeVariables
        overlay(prefix, items.keySet(), hostConfig, merged);
        for (Map<String, String> values : dynamicValues) {
            overlay(prefix, items.keySet(), values, merged);
        }
        overlayEnvironment(pluginId + nestedPath, items.keySet(), merged);
        overlay(prefix, items.keySet(), systemProperties, merged);
        overlay(prefix, items.keySet(), runtimeVariables, merged);

        Map<String, Object> typed = new LinkedHashMap<>();
        Map<String, String> display = new LinkedHashMap<>();
        for (ConfigItemDefinition item : definitions) {
            String raw = merged.get(item.key());
            if ((raw == null || raw.isBlank()) && item.required()) {
                throw invalid("required " + scopeName + " config is missing: " + item.key());
            }
            if (raw != null) {
                typed.put(item.key(), convert(item, raw));
                // 诊断只显示是否设置，绝不输出配置值或 Secret。
                display.put(item.key(), item.secret() ? "<secret>" : "<set>");
            }
        }
        return new DefaultPluginConfig(typed, display);
    }

    private static void registerEnvironmentName(Map<String, String> names, String fullKey, String environmentName) {
        String previous = names.putIfAbsent(environmentName, fullKey);
        if (previous != null && !previous.equals(fullKey)) {
            throw invalid("plugin config environment name conflict: " + previous + ", " + fullKey);
        }
    }

    private static List<ConfigSource> immutableSources(List<ConfigSource> source) {
        if (source == null) {
            return List.of();
        }
        for (ConfigSource configSource : source) {
            if (configSource == null) {
                throw invalid("config sources must not contain null entries");
            }
        }
        return List.copyOf(source);
    }

    private static Map<String, String> immutable(Map<String, String> source) {
        if (source == null) {
            return Map.of();
        }
        for (Map.Entry<String, String> entry : source.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                throw invalid("configuration source keys and values must not be null");
            }
        }
        return Map.copyOf(source);
    }

    private static void rejectUnknown(
            String prefix,
            Set<String> knownKeys,
            Map<String, String> source
    ) {
        for (String fullKey : source.keySet()) {
            if (fullKey.startsWith(prefix) && !knownKeys.contains(fullKey.substring(prefix.length()))) {
                throw invalid("unknown plugin config key: " + fullKey);
            }
        }
    }

    private static void overlay(
            String prefix,
            Set<String> knownKeys,
            Map<String, String> source,
            Map<String, String> target
    ) {
        for (String key : knownKeys) {
            String value = source.get(prefix + key);
            if (value != null) {
                target.put(key, value);
            }
        }
    }

    private void overlayEnvironment(String pluginId, Set<String> knownKeys, Map<String, String> target) {
        Map<String, String> environmentNames = new HashMap<>();
        for (String key : knownKeys) {
            String name = environmentName(pluginId, key);
            String previous = environmentNames.putIfAbsent(name, key);
            if (previous != null && !previous.equals(key)) {
                throw invalid("plugin config environment name conflict: " + previous + ", " + key);
            }
            String value = environment.get(name);
            if (value != null) {
                target.put(key, value);
            }
        }
    }

    private static Object convert(ConfigItemDefinition item, String raw) {
        try {
            return switch (item.type()) {
                case STRING -> raw;
                case INTEGER -> Integer.valueOf(raw);
                case LONG -> Long.valueOf(raw);
                case BOOLEAN -> parseBoolean(item.key(), raw);
                case DECIMAL -> new BigDecimal(raw);
                case DURATION -> Duration.parse(raw);
                case URI -> parseUri(item.key(), raw);
                case ENUM -> parseEnum(item, raw);
                case SECRET -> SecretValue.of(raw);
            };
        } catch (NumberFormatException | DateTimeParseException | URISyntaxException exception) {
            throw NexusException.build(
                    PluginStatusCode.PLUGIN_CONFIG_INVALID.fullCode(),
                    "cannot convert plugin config: " + item.key(),
                    exception);
        }
    }

    private static boolean parseBoolean(String key, String raw) {
        if ("true".equalsIgnoreCase(raw)) {
            return true;
        }
        if ("false".equalsIgnoreCase(raw)) {
            return false;
        }
        throw invalid("cannot convert plugin config to boolean: " + key);
    }

    private static URI parseUri(String key, String raw) throws URISyntaxException {
        URI uri = new URI(raw).normalize();
        if (!uri.isAbsolute()) {
            throw invalid("plugin URI config must be absolute: " + key);
        }
        return uri;
    }

    private static String parseEnum(ConfigItemDefinition item, String raw) {
        if (!item.enumValues().contains(raw)) {
            throw invalid("plugin enum config value is not allowed: " + item.key());
        }
        return raw;
    }

    private static NexusException invalid(String message) {
        return NexusException.build(PluginStatusCode.PLUGIN_CONFIG_INVALID, message);
    }
}
