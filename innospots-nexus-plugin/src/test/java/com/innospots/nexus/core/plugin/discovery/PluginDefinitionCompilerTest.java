package com.innospots.nexus.core.plugin.discovery;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.capability.CapabilityTypeRegistry;
import com.innospots.nexus.core.plugin.contract.CapabilityProvider;
import com.innospots.nexus.core.plugin.declaration.JacksonPluginManifestParser;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;
import com.innospots.nexus.core.plugin.declaration.PluginSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** 验证 YAML Capability Java binding 的编译和延迟实例化语义。 */
class PluginDefinitionCompilerTest {

    @Test
    void compilesExplicitJavaBindingWithoutCreatingProvider() {
        PluginDefinition definition = compiler().compile(new JacksonPluginManifestParser().parse("""
                apiVersion: nexus.plugin/v1
                kind: Plugin
                metadata:
                  pluginId: com.example.compiler
                  version: 1.0.0
                spec:
                  apiVersion: 1
                  displayName: {en: Compiler}
                  capabilities:
                    - type: test.provider
                      majorVersion: 1
                      providerId: test
                      api: com.innospots.nexus.core.plugin.discovery.PluginDefinitionCompilerTest$TestProvider
                      bind: {kind: java, class: com.innospots.nexus.core.plugin.discovery.PluginDefinitionCompilerTest$Provider}
                """), PluginSource.yaml("memory:test", Instant.now()));

        assertThat(definition.pluginId()).isEqualTo("com.example.compiler");
        assertThat(definition.capabilities()).hasSize(1);
    }

    @Test
    void registersCapabilityTypeFromYamlApiField() {
        CapabilityTypeRegistry.Builder types = CapabilityTypeRegistry.builder();
        PluginDefinitionCompiler compiler = new PluginDefinitionCompiler(
                types, PluginDefinitionCompilerTest.class.getClassLoader());
        compiler.compile(new JacksonPluginManifestParser().parse("""
                apiVersion: nexus.plugin/v1
                kind: Plugin
                metadata: {pluginId: com.example.compiler, version: 1.0.0}
                spec:
                  apiVersion: 1
                  displayName: {en: Compiler}
                  capabilities:
                    - type: test.provider
                      majorVersion: 1
                      providerId: test
                      api: com.innospots.nexus.core.plugin.discovery.PluginDefinitionCompilerTest$TestProvider
                      bind: {kind: java, class: com.innospots.nexus.core.plugin.discovery.PluginDefinitionCompilerTest$Provider}
                """), PluginSource.yaml("memory:test", Instant.now()));

        assertThat(types.find("test.provider", 1)).isPresent();
    }

    @Test
    void resolvesRequirementsAfterCapabilityTypesAreRegistered() {
        CapabilityTypeRegistry.Builder types = CapabilityTypeRegistry.builder();
        PluginDefinitionCompiler compiler = new PluginDefinitionCompiler(
                types, PluginDefinitionCompilerTest.class.getClassLoader());
        var parser = new JacksonPluginManifestParser();
        var providerManifest = parser.parse("""
                apiVersion: nexus.plugin/v1
                kind: Plugin
                metadata: {pluginId: com.example.provider, version: 1.0.0}
                spec:
                  apiVersion: 1
                  displayName: {en: Provider}
                  capabilities:
                    - type: test.provider
                      majorVersion: 1
                      providerId: test
                      api: com.innospots.nexus.core.plugin.discovery.PluginDefinitionCompilerTest$TestProvider
                      bind: {kind: java, class: com.innospots.nexus.core.plugin.discovery.PluginDefinitionCompilerTest$Provider}
                """);
        compiler.registerDeclaredTypes(providerManifest);
        PluginDefinition consumer = compiler.compile(parser.parse("""
                apiVersion: nexus.plugin/v1
                kind: Plugin
                metadata: {pluginId: com.example.consumer, version: 1.0.0}
                spec:
                  apiVersion: 1
                  displayName: {en: Consumer}
                  requirements:
                    - type: test.provider
                      majorVersion: 1
                      required: true
                  capabilities:
                    - type: test.provider
                      majorVersion: 1
                      providerId: consumer
                      api: com.innospots.nexus.core.plugin.discovery.PluginDefinitionCompilerTest$TestProvider
                      bind: {kind: java, class: com.innospots.nexus.core.plugin.discovery.PluginDefinitionCompilerTest$Provider}
                """), PluginSource.yaml("memory:consumer", Instant.now()));

        assertThat(consumer.requirements()).hasSize(1);
    }

    @Test
    void rejectsMissingApiAndUnsupportedBind() {
        String prefix = """
                apiVersion: nexus.plugin/v1
                kind: Plugin
                metadata: {pluginId: com.example.compiler, version: 1.0.0}
                spec:
                  apiVersion: 1
                  displayName: {en: Compiler}
                  capabilities:
                    - type: %s
                      majorVersion: 1
                      providerId: test
                      %s
                      bind: {kind: %s, class: example.Provider}
                """;
        assertThatThrownBy(() -> compiler().compile(
                new JacksonPluginManifestParser().parse(prefix.formatted(
                        "test.provider", "", "java")),
                PluginSource.yaml("memory:test", Instant.now())))
                .isInstanceOf(NexusException.class);
        assertThatThrownBy(() -> compiler().compile(
                new JacksonPluginManifestParser().parse(prefix.formatted(
                        "test.provider",
                        "api: com.innospots.nexus.core.plugin.discovery.PluginDefinitionCompilerTest$TestProvider",
                        "http")),
                PluginSource.yaml("memory:test", Instant.now())))
                .isInstanceOf(NexusException.class);
    }

    @Test
    void compilesAllConfigurationTypesDefinedByTheDsl() {
        PluginDefinition definition = compiler().compile(new JacksonPluginManifestParser().parse("""
                apiVersion: nexus.plugin/v1
                kind: Plugin
                metadata: {pluginId: com.example.config-types, version: 1.0.0}
                spec:
                  apiVersion: 1
                  displayName: {en: Config Types}
                  config:
                    - {key: threshold, type: DECIMAL, default: 0.75}
                    - {key: endpoint, type: URI, default: https://example.com/api}
                    - key: mode
                      type: ENUM
                      enumValues: [fast, safe]
                      default: safe
                  capabilities:
                    - type: test.provider
                      majorVersion: 1
                      providerId: test
                      api: com.innospots.nexus.core.plugin.discovery.PluginDefinitionCompilerTest$TestProvider
                      bind: {kind: java, class: com.innospots.nexus.core.plugin.discovery.PluginDefinitionCompilerTest$Provider}
                """), PluginSource.yaml("memory:config-types", Instant.now()));

        assertThat(definition.config().items()).hasSize(3);
    }

    private static PluginDefinitionCompiler compiler() {
        return new PluginDefinitionCompiler(
                CapabilityTypeRegistry.builder(),
                PluginDefinitionCompilerTest.class.getClassLoader());
    }

    public interface TestProvider extends CapabilityProvider {
    }

    public static final class Provider implements TestProvider {
        public Provider() {
        }
    }
}
