package com.innospots.nexus.core.plugin.discovery;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.capability.CapabilityTypeRegistry;
import com.innospots.nexus.core.plugin.contract.Plugin;
import com.innospots.nexus.core.plugin.contribution.PluginContributionDecoderRegistry;
import com.innospots.nexus.core.plugin.declaration.JacksonPluginManifestParser;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;
import com.innospots.nexus.core.plugin.declaration.PluginManifest;
import com.innospots.nexus.core.plugin.declaration.PluginManifestParser;
import com.innospots.nexus.core.plugin.declaration.PluginSource;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 通过 Java SPI 和全部 plugin.yaml 资源发现插件，并统一编译为 Plugin。 */
public final class ClasspathPluginDiscovery {

    private static final Logger logger = LoggerFactory.getLogger(ClasspathPluginDiscovery.class);

    /** YAML 资源标准路径。 */
    public static final String MANIFEST_RESOURCE = "META-INF/nexus/plugin.yaml";

    private final ClassLoader classLoader;
    private final PluginContributionDecoderRegistry contributionDecoders;
    private final PluginManifestParser manifestParser;

    /**
     * 使用空 Contribution Decoder 表创建发现器。
     *
     * @param classLoader 用于 SPI 与 YAML 资源枚举的类加载器
     * @throws com.innospots.nexus.base.exception.NexusException 依赖为 {@code null} 时
     */
    public ClasspathPluginDiscovery(ClassLoader classLoader) {
        this(classLoader, PluginContributionDecoderRegistry.builder().build(), new JacksonPluginManifestParser());
    }

    /**
     * 使用宿主注册的 Contribution Decoder 编译 YAML。
     *
     * @param classLoader          类加载器
     * @param contributionDecoders 已注册的 Contribution 解码器
     * @throws com.innospots.nexus.base.exception.NexusException 依赖为 {@code null} 时
     */
    public ClasspathPluginDiscovery(
            ClassLoader classLoader,
            PluginContributionDecoderRegistry contributionDecoders
    ) {
        this(classLoader, contributionDecoders, new JacksonPluginManifestParser());
    }

    /**
     * 使用自定义解析器创建发现器，便于宿主注入受控解析策略。
     *
     * @param classLoader          类加载器
     * @param contributionDecoders 已注册的 Contribution 解码器
     * @param manifestParser       YAML 清单解析器
     * @throws com.innospots.nexus.base.exception.NexusException 依赖为 {@code null} 时
     */
    public ClasspathPluginDiscovery(
            ClassLoader classLoader,
            PluginContributionDecoderRegistry contributionDecoders,
            PluginManifestParser manifestParser
    ) {
        if (classLoader == null || contributionDecoders == null || manifestParser == null) {
            throw NexusException.build(PluginStatusCode.PLUGIN_DISCOVERY_FAILED,
                    "plugin classLoader and discovery services are required");
        }
        this.classLoader = classLoader;
        this.contributionDecoders = contributionDecoders;
        this.manifestParser = manifestParser;
    }

    /**
     * 返回有效目录与单插件拒绝诊断。
     *
     * @return 发现报告，包含有效目录和拒绝列表
     */
    public PluginDiscoveryReport discoverReport() {
        List<DiscoveredPlugin> discovered = new ArrayList<>();
        List<RejectedPluginDefinition> rejected = new ArrayList<>();
        CapabilityTypeRegistry.Builder capabilityTypes = CapabilityTypeRegistry.builder();
        discoverJava(discovered, rejected, capabilityTypes);
        PluginDefinitionCompiler compiler = new PluginDefinitionCompiler(
                capabilityTypes, contributionDecoders, classLoader);
        discoverYaml(discovered, rejected, compiler);
        PluginCatalog catalog = PluginCatalog.of(discovered);
        logger.info("Plugin discovery completed: valid={}, rejected={}",
                discovered.size(), rejected.size());
        if (logger.isDebugEnabled()) {
            discovered.forEach(item -> logger.debug("Discovered plugin: id={}, source={}, version={}",
                    item.definition().pluginId(),
                    item.source().sourceType(),
                    item.definition().version()));
        }
        rejected.forEach(item -> logger.warn(
                "Rejected plugin definition: source={}, claimedPluginId={}, reason={}",
                item.source().location(),
                item.claimedPluginId(),
                item.diagnostics().isEmpty() ? "unknown" : item.diagnostics().getFirst()));
        return new PluginDiscoveryReport(catalog, rejected);
    }

    private void discoverJava(
            List<DiscoveredPlugin> discovered,
            List<RejectedPluginDefinition> rejected,
            CapabilityTypeRegistry.Builder capabilityTypes
    ) {
        for (ServiceLoader.Provider<Plugin> provider : ServiceLoader.load(Plugin.class, classLoader).stream().toList()) {
            Instant now = Instant.now();
            PluginSource source = PluginSource.java(provider.type().getName(), now);
            try {
                Plugin plugin = provider.get();
                PluginDefinition definition = plugin.definition();
                if (definition == null) {
                    throw invalid("plugin returned null definition: " + provider.type().getName());
                }
                capabilityTypes.registerFrom(definition);
                discovered.add(new DiscoveredPlugin(plugin, definition, now, source));
            } catch (NexusException exception) {
                rejected.add(new RejectedPluginDefinition(source, null, List.of(exception.getMessage())));
            } catch (ServiceConfigurationError | RuntimeException | LinkageError exception) {
                rejected.add(new RejectedPluginDefinition(source, null, List.of(exception.toString())));
            }
        }
    }

    private void discoverYaml(
            List<DiscoveredPlugin> discovered,
            List<RejectedPluginDefinition> rejected,
            PluginDefinitionCompiler compiler
    ) {
        List<ParsedYamlManifest> pending = new ArrayList<>();
        try {
            Enumeration<URL> resources = classLoader.getResources(MANIFEST_RESOURCE);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                Instant now = Instant.now();
                PluginSource source = PluginSource.yaml(resource.toExternalForm(), now);
                try (InputStream input = resource.openStream()) {
                    PluginManifest manifest = manifestParser.parse(input);
                    // 第一遍：登记全部 YAML capability 的 api 类型，供跨插件 requirements 解析。
                    compiler.registerDeclaredTypes(manifest);
                    pending.add(new ParsedYamlManifest(manifest, source, now));
                } catch (NexusException exception) {
                    rejected.add(new RejectedPluginDefinition(source, null, List.of(exception.getMessage())));
                } catch (IOException | RuntimeException | LinkageError exception) {
                    rejected.add(new RejectedPluginDefinition(source, null, List.of(exception.toString())));
                }
            }
            // 第二遍：在完整类型表就绪后编译各 manifest，避免 requirements 引用尚未登记的 capability。
            for (ParsedYamlManifest item : pending) {
                try {
                    PluginDefinition definition = compiler.compile(item.manifest(), item.source());
                    discovered.add(new DiscoveredPlugin(
                            new ManifestPlugin(definition), definition, item.discoveredAt(), item.source()));
                } catch (NexusException exception) {
                    rejected.add(new RejectedPluginDefinition(item.source(), null, List.of(exception.getMessage())));
                } catch (RuntimeException | LinkageError exception) {
                    rejected.add(new RejectedPluginDefinition(item.source(), null, List.of(exception.toString())));
                }
            }
        } catch (IOException exception) {
            throw NexusException.build(PluginStatusCode.PLUGIN_DISCOVERY_FAILED.fullCode(),
                    "cannot enumerate plugin YAML resources", exception);
        }
    }

    private record ParsedYamlManifest(PluginManifest manifest, PluginSource source, Instant discoveredAt) {
    }

    private static NexusException invalid(String message) {
        return NexusException.build(PluginStatusCode.PLUGIN_DEFINITION_INVALID, message);
    }
}
