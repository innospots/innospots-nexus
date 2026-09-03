package com.innospots.nexus.core.plugin.discovery;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.core.plugin.capability.CapabilityType;
import com.innospots.nexus.core.plugin.capability.CapabilityTypeRegistry;
import com.innospots.nexus.core.plugin.capability.Tags;
import com.innospots.nexus.core.plugin.config.ConfigDefinition;
import com.innospots.nexus.core.plugin.config.ConfigItemDefinition;
import com.innospots.nexus.core.plugin.config.ConfigType;
import com.innospots.nexus.core.plugin.contract.CapabilityProvider;
import com.innospots.nexus.core.plugin.contract.CapabilityProviderFactory;
import com.innospots.nexus.core.plugin.contribution.PluginContribution;
import com.innospots.nexus.core.plugin.contribution.PluginContributionDecoder;
import com.innospots.nexus.core.plugin.contribution.PluginContributionDecoderRegistry;
import com.innospots.nexus.core.plugin.declaration.CapabilityContribution;
import com.innospots.nexus.core.plugin.declaration.CapabilityRequirement;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;
import com.innospots.nexus.core.plugin.declaration.PluginManifest;
import com.innospots.nexus.core.plugin.declaration.PluginSource;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/** 将 YAML 纯数据模型编译为无副作用的运行时 PluginDefinition。 */
public final class PluginDefinitionCompiler {

    private final CapabilityTypeRegistry.Builder capabilityTypes;
    private final PluginContributionDecoderRegistry contributionDecoders;
    private final ClassLoader classLoader;

    /**
     * 创建只支持 Capability Java binding 的编译器。
     *
     * @param capabilityTypes 发现阶段累积的 Capability 类型表
     * @param classLoader 加载实现类的类加载器
     * @throws NexusException 类型表或类加载器为空时抛出
     */
    public PluginDefinitionCompiler(CapabilityTypeRegistry.Builder capabilityTypes, ClassLoader classLoader) {
        this(capabilityTypes, PluginContributionDecoderRegistry.builder().build(), classLoader);
    }

    /**
     * 创建带显式 Contribution Decoder 的编译器。
     *
     * @param capabilityTypes 发现阶段累积的 Capability 类型表
     * @param contributionDecoders 宿主注册的 Contribution Decoder 表
     * @param classLoader 加载实现类的类加载器
     * @throws NexusException 任一依赖为空时抛出
     */
    public PluginDefinitionCompiler(
            CapabilityTypeRegistry.Builder capabilityTypes,
            PluginContributionDecoderRegistry contributionDecoders,
            ClassLoader classLoader
    ) {
        if (capabilityTypes == null || contributionDecoders == null || classLoader == null) {
            throw NexusException.build(PluginStatusCode.PLUGIN_DEFINITION_INVALID,
                    "compiler registries and classLoader are required");
        }
        this.capabilityTypes = capabilityTypes;
        this.contributionDecoders = contributionDecoders;
        this.classLoader = classLoader;
    }

    /**
     * 从 YAML capability 声明登记类型，供跨插件 requirements 解析。
     *
     * @param manifest 已解析的插件清单
     * @throws NexusException capability 缺少 api 或类型冲突时抛出
     */
    public void registerDeclaredTypes(PluginManifest manifest) {
        if (manifest == null || manifest.spec() == null) {
            return;
        }
        for (PluginManifest.Capability capability : manifest.spec().capabilities()) {
            registerDeclaredType(capability);
        }
    }

    /**
     * 编译一个 YAML 插件定义。
     *
     * @param manifest 已解析的插件清单
     * @param source 声明来源元数据
     * @return 无副作用的运行时插件定义
     * @throws NexusException DSL 结构、绑定或类型解析失败时抛出
     */
    public PluginDefinition compile(PluginManifest manifest, PluginSource source) {
        if (manifest == null || source == null || manifest.metadata() == null || manifest.spec() == null) {
            throw invalid(PluginStatusCode.DSL_STRUCTURE_INVALID, "plugin manifest structure is invalid");
        }
        // 1. 校验 DSL 文档头与协议版本。
        if (!"nexus.plugin/v1".equals(manifest.apiVersion()) || !"Plugin".equals(manifest.kind())) {
            throw invalid(PluginStatusCode.DSL_STRUCTURE_INVALID, "unsupported plugin DSL header");
        }
        PluginManifest.Metadata metadata = manifest.metadata();
        PluginManifest.Spec spec = manifest.spec();
        if (metadata.pluginId() == null || metadata.version() == null
                || spec.apiVersion() == null) {
            throw invalid(PluginStatusCode.DSL_STRUCTURE_INVALID,
                    "plugin metadata and spec apiVersion are required");
        }
        if (spec.apiVersion() != PluginDefinition.CURRENT_API_VERSION) {
            throw invalid(PluginStatusCode.PLUGIN_API_INCOMPATIBLE, "unsupported plugin apiVersion");
        }
        // 2. 编译 Capability Provider 声明并登记类型。
        List<CapabilityContribution<?>> capabilities = new ArrayList<>();
        for (PluginManifest.Capability capability : spec.capabilities()) {
            capabilities.add(compileCapability(capability));
        }
        // 3. 解析跨插件 Capability 依赖。
        List<CapabilityRequirement> requirements = new ArrayList<>();
        for (PluginManifest.Requirement requirement : spec.requirements()) {
            if (requirement == null || requirement.type() == null || requirement.majorVersion() == null) {
                throw invalid(PluginStatusCode.DSL_STRUCTURE_INVALID,
                        "capability requirement type and majorVersion are required");
            }
            CapabilityType<?> type = resolveType(requirement.type(), requirement.majorVersion());
            requirements.add(new CapabilityRequirement(
                    type.key(),
                    Tags.from(requirement.tags()),
                    requirement.required() == null || requirement.required()));
        }
        // 4. 解码通用 Contribution 声明。
        List<PluginContribution> contributions = new ArrayList<>();
        for (Map<String, Object> declaration : spec.contributions()) {
            contributions.add(decodeContribution(declaration));
        }
        // 5. 要求插件至少提供一种可运行能力。
        if (capabilities.isEmpty() && contributions.isEmpty()) {
            throw invalid(PluginStatusCode.DSL_STRUCTURE_INVALID, "plugin must declare capability or contribution");
        }
        // 6. 组装并校验不可变 PluginDefinition。
        return new PluginDefinition(
                metadata.pluginId(),
                metadata.version(),
                spec.apiVersion(),
                i18n(spec.displayName(), "displayName"),
                spec.description().isEmpty() ? I18nObject.of(Map.of()) : i18n(spec.description(), "description"),
                Tags.from(spec.tags()),
                config(spec.config()),
                capabilities,
                requirements,
                contributions);
    }

    private CapabilityContribution<?> compileCapability(PluginManifest.Capability declaration) {
        if (declaration == null || declaration.type() == null || declaration.providerId() == null
                || declaration.majorVersion() == null || declaration.bind() == null) {
            throw invalid(PluginStatusCode.DSL_STRUCTURE_INVALID, "capability declaration is incomplete");
        }
        CapabilityType<?> type = registerDeclaredType(declaration);
        if (declaration.exposures() != null && !declaration.exposures().isEmpty()) {
            throw invalid(PluginStatusCode.UNSUPPORTED_EXPOSURE_KIND, "capability exposures are not supported");
        }
        if (!"java".equals(declaration.bind().kind())) {
            throw invalid(PluginStatusCode.UNSUPPORTED_BIND_KIND,
                    "unsupported bind kind: " + declaration.bind().kind());
        }
        Class<?> implementation;
        try {
            implementation = Class.forName(declaration.bind().className(), false, classLoader);
        } catch (ClassNotFoundException | LinkageError exception) {
            throw NexusException.build(PluginStatusCode.PLUGIN_DEFINITION_INVALID.fullCode(),
                    "capability implementation class cannot be loaded: " + declaration.bind().className(), exception);
        }
        if (!Modifier.isPublic(implementation.getModifiers())
                || implementation.isInterface() || Modifier.isAbstract(implementation.getModifiers())
                || !CapabilityProvider.class.isAssignableFrom(implementation)
                || !type.api().isAssignableFrom(implementation)) {
            throw invalid(PluginStatusCode.CAPABILITY_TYPE_MISMATCH,
                    "capability implementation does not implement API: " + declaration.bind().className());
        }
        Constructor<?> constructor;
        try {
            constructor = implementation.getConstructor();
        } catch (NoSuchMethodException exception) {
            throw NexusException.build(PluginStatusCode.PLUGIN_DEFINITION_INVALID.fullCode(),
                    "capability implementation needs a public no-arg constructor: "
                            + declaration.bind().className(), exception);
        }
        CapabilityProviderFactory<CapabilityProvider> factory = () -> instantiate(constructor);
        return contribution(type, declaration, factory);
    }

    private CapabilityType<?> registerDeclaredType(PluginManifest.Capability declaration) {
        if (declaration == null || declaration.type() == null || declaration.majorVersion() == null) {
            throw invalid(PluginStatusCode.DSL_STRUCTURE_INVALID, "capability type and majorVersion are required");
        }
        if (declaration.api() == null || declaration.api().isBlank()) {
            throw invalid(PluginStatusCode.DSL_STRUCTURE_INVALID,
                    "capability api is required: " + declaration.type() + "@" + declaration.majorVersion());
        }
        Class<?> apiClass = loadApiClass(declaration.api());
        CapabilityType<?> declared = CapabilityType.of(
                declaration.type(),
                declaration.majorVersion(),
                asCapabilityApi(apiClass));
        capabilityTypes.register(declared);
        return capabilityTypes.find(declaration.type(), declaration.majorVersion()).orElse(declared);
    }

    @SuppressWarnings("unchecked")
    private <T extends CapabilityProvider> Class<T> asCapabilityApi(Class<?> apiClass) {
        if (!apiClass.isInterface() || !CapabilityProvider.class.isAssignableFrom(apiClass)) {
            throw invalid(PluginStatusCode.CAPABILITY_TYPE_MISMATCH,
                    "capability API must be an interface extending CapabilityProvider: " + apiClass.getName());
        }
        return (Class<T>) apiClass;
    }

    private Class<?> loadApiClass(String className) {
        try {
            return Class.forName(className, false, classLoader);
        } catch (ClassNotFoundException | LinkageError exception) {
            throw NexusException.build(PluginStatusCode.PLUGIN_DEFINITION_INVALID.fullCode(),
                    "capability API class cannot be loaded: " + className, exception);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static CapabilityContribution<?> contribution(
            CapabilityType<?> type,
            PluginManifest.Capability declaration,
            CapabilityProviderFactory<CapabilityProvider> factory
    ) {
        return new CapabilityContribution(
                type,
                declaration.providerId(),
                Tags.from(declaration.tags()),
                config(declaration.config()),
                factory);
    }

    @SuppressWarnings("unchecked")
    private static CapabilityProvider instantiate(Constructor<?> constructor) {
        try {
            return (CapabilityProvider) constructor.newInstance();
        } catch (ReflectiveOperationException | RuntimeException exception) {
            throw NexusException.build(PluginStatusCode.PLUGIN_START_FAILED.fullCode(),
                    "cannot instantiate capability provider", exception);
        }
    }

    private CapabilityType<?> resolveType(String name, Integer majorVersion) {
        if (name == null || name.isBlank() || majorVersion == null || majorVersion < 1) {
            throw invalid(PluginStatusCode.DSL_STRUCTURE_INVALID,
                    "capability type and positive majorVersion are required");
        }
        return capabilityTypes.find(name, majorVersion).orElseThrow(() -> invalid(
                PluginStatusCode.CAPABILITY_TYPE_UNKNOWN,
                "unknown capability type: " + name + "@" + majorVersion));
    }

    private PluginContribution decodeContribution(Map<String, Object> declaration) {
        if (declaration == null) {
            throw invalid(PluginStatusCode.DSL_STRUCTURE_INVALID, "contribution declaration is null");
        }
        Object typeValue = declaration.get("type");
        if (!(typeValue instanceof String name) || name.isBlank()) {
            throw invalid(PluginStatusCode.DSL_STRUCTURE_INVALID,
                    "contribution type must be a non-empty string");
        }
        Object majorValue = declaration.get("majorVersion");
        if (!(majorValue instanceof Number number)) {
            throw invalid(PluginStatusCode.DSL_STRUCTURE_INVALID,
                    "contribution majorVersion must be a positive integer");
        }
        int major = number.intValue();
        if (major < 1 || number.doubleValue() != major) {
            throw invalid(PluginStatusCode.DSL_STRUCTURE_INVALID,
                    "contribution majorVersion must be a positive integer");
        }
        var type = new com.innospots.nexus.core.plugin.contribution.PluginContributionType<>(name, major);
        PluginContributionDecoder<?> decoder = contributionDecoders.find(type).orElseThrow(() -> invalid(
                PluginStatusCode.UNSUPPORTED_CONTRIBUTION_TYPE,
                "unsupported contribution type: " + type));
        return decode(decoder, declaration);
    }

    @SuppressWarnings("unchecked")
    private static <T extends PluginContribution> T decode(
            PluginContributionDecoder<?> decoder,
            Map<String, Object> declaration
    ) {
        return ((PluginContributionDecoder<T>) decoder).decode(Map.copyOf(declaration));
    }

    private static ConfigDefinition config(List<PluginManifest.ConfigItem> source) {
        List<ConfigItemDefinition> items = source.stream().map(PluginDefinitionCompiler::configItem).toList();
        return () -> items;
    }

    private static ConfigItemDefinition configItem(PluginManifest.ConfigItem item) {
        if (item == null || item.key() == null || item.type() == null) {
            throw invalid(PluginStatusCode.DSL_STRUCTURE_INVALID, "config item is incomplete");
        }
        ConfigType type;
        try {
            type = ConfigType.valueOf(item.type());
        } catch (IllegalArgumentException exception) {
            throw NexusException.build(PluginStatusCode.DSL_STRUCTURE_INVALID.fullCode(),
                    "unsupported config type: " + item.type(), exception);
        }
        if (type == ConfigType.SECRET && item.defaultValue() != null) {
            throw invalid(PluginStatusCode.DSL_STRUCTURE_INVALID, "secret config cannot have default");
        }
        if (type == ConfigType.ENUM && item.enumValues().isEmpty()) {
            throw invalid(PluginStatusCode.DSL_STRUCTURE_INVALID,
                    "ENUM config must declare enumValues: " + item.key());
        }
        if (type != ConfigType.ENUM && !item.enumValues().isEmpty()) {
            throw invalid(PluginStatusCode.DSL_STRUCTURE_INVALID,
                    "only ENUM config may declare enumValues: " + item.key());
        }
        validateDefault(item, type);
        return new ConfigItemDefinition(
                item.key(),
                type,
                Boolean.TRUE.equals(item.required()),
                item.defaultText(),
                type == ConfigType.SECRET,
                item.description(),
                item.enumValues());
    }

    /** 按 DSL 声明类型校验默认值，避免错误配置延迟到插件启动后才暴露。 */
    private static void validateDefault(PluginManifest.ConfigItem item, ConfigType type) {
        Object value = item.defaultValue();
        if (value == null) {
            return;
        }
        boolean scalarType = switch (type) {
            case STRING, DURATION, URI, ENUM -> value instanceof String;
            case BOOLEAN -> value instanceof Boolean;
            case INTEGER, LONG -> integralNumber(value);
            case DECIMAL -> value instanceof Number || value instanceof String;
            case SECRET -> false;
        };
        if (!scalarType) {
            throw invalid(PluginStatusCode.DSL_STRUCTURE_INVALID,
                    "default value has the wrong type: " + item.key());
        }
        try {
            switch (type) {
                case INTEGER -> range(value, Integer.MIN_VALUE, Integer.MAX_VALUE, item.key());
                case LONG -> range(value, Long.MIN_VALUE, Long.MAX_VALUE, item.key());
                case DECIMAL -> new BigDecimal(String.valueOf(value));
                case DURATION -> Duration.parse((String) value);
                case URI -> {
                    URI uri = URI.create((String) value).normalize();
                    if (!uri.isAbsolute()) {
                        throw invalid(PluginStatusCode.DSL_STRUCTURE_INVALID,
                                "URI default must be absolute: " + item.key());
                    }
                }
                case ENUM -> {
                    if (!item.enumValues().contains(value)) {
                        throw invalid(PluginStatusCode.DSL_STRUCTURE_INVALID,
                                "ENUM default is not listed in enumValues: " + item.key());
                    }
                }
                default -> {
                    // STRING、BOOLEAN 和 SECRET 已由上面的类型规则完成校验。
                }
            }
        } catch (IllegalArgumentException exception) {
            throw NexusException.build(PluginStatusCode.DSL_STRUCTURE_INVALID.fullCode(),
                    "default value cannot be converted: " + item.key(), exception);
        }
    }

    private static boolean integralNumber(Object value) {
        if (!(value instanceof Number)) {
            return false;
        }
        try {
            return new BigDecimal(String.valueOf(value)).stripTrailingZeros().scale() <= 0;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    private static void range(Object value, long minimum, long maximum, String key) {
        BigDecimal number = new BigDecimal(String.valueOf(value));
        if (number.compareTo(BigDecimal.valueOf(minimum)) < 0
                || number.compareTo(BigDecimal.valueOf(maximum)) > 0) {
            throw invalid(PluginStatusCode.DSL_STRUCTURE_INVALID,
                    "integer default is out of range: " + key);
        }
    }

    private static I18nObject i18n(Map<String, String> values, String field) {
        if (values == null || values.isEmpty()) {
            throw invalid(PluginStatusCode.DSL_STRUCTURE_INVALID, field + " must not be empty");
        }
        return I18nObject.of(values);
    }

    private static NexusException invalid(PluginStatusCode code, String message) {
        return NexusException.build(code, message);
    }
}
