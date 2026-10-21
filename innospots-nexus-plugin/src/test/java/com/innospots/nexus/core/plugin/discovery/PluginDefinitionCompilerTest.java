package com.innospots.nexus.core.plugin.discovery;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.contribution.PluginContributionDecoderRegistry;
import com.innospots.nexus.core.plugin.contribution.console.ConsolePluginContributionDecoder;
import com.innospots.nexus.core.plugin.declaration.JacksonPluginManifestParser;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;
import com.innospots.nexus.core.plugin.declaration.PluginSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** 验证 YAML 插件清单编译语义。 */
class PluginDefinitionCompilerTest {

    @Test
    void compilesConsoleContributionFromYaml() {
        PluginDefinition definition = compiler().compile(new JacksonPluginManifestParser().parse("""
                apiVersion: nexus.plugin/v1
                kind: Plugin
                metadata:
                  pluginId: com.example.compiler
                  version: 1.0.0
                spec:
                  apiVersion: 1
                  displayName: {en: Compiler}
                  contributions:
                    - type: console
                      majorVersion: 2
                      domainKey: nexus
                      modules:
                        - moduleKey: main
                          displayName: {en: Main}
                          orderIndex: 0
                          viewGroups:
                            - group: default
                              label: {en: Views}
                          operationGroups:
                            - group: default
                              label: {en: Ops}
                          children:
                            - displayName: {en: Menu}
                              orderIndex: 0
                              entryPageKey: compiler-page
                              children: []
                """), PluginSource.yaml("memory:test", Instant.now()));

        assertThat(definition.pluginId()).isEqualTo("com.example.compiler");
        assertThat(definition.contributions()).hasSize(1);
    }

    @Test
    void rejectsLegacyCapabilities() {
        assertThatThrownBy(() -> compiler().compile(new JacksonPluginManifestParser().parse("""
                apiVersion: nexus.plugin/v1
                kind: Plugin
                metadata: {pluginId: com.example.legacy, version: 1.0.0}
                spec:
                  apiVersion: 1
                  displayName: {en: Legacy}
                  capabilities:
                    - type: test.provider
                      majorVersion: 1
                      providerId: test
                      api: java.lang.Runnable
                      bind: {kind: java, class: java.lang.Object}
                """), PluginSource.yaml("memory:legacy", Instant.now())))
                .isInstanceOf(NexusException.class)
                .hasMessageContaining("capabilities are no longer supported");
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
                  contributions:
                    - type: console
                      majorVersion: 2
                      domainKey: nexus
                      modules:
                        - moduleKey: main
                          displayName: {en: Main}
                          orderIndex: 0
                          viewGroups:
                            - group: default
                              label: {en: Views}
                          operationGroups:
                            - group: default
                              label: {en: Ops}
                          children:
                            - displayName: {en: Menu}
                              orderIndex: 0
                              entryPageKey: compiler-page
                              children: []
                """), PluginSource.yaml("memory:config-types", Instant.now()));

        assertThat(definition.config().items()).hasSize(3);
    }

    private static PluginDefinitionCompiler compiler() {
        return new PluginDefinitionCompiler(
                PluginContributionDecoderRegistry.builder()
                        .register(new ConsolePluginContributionDecoder())
                        .build(),
                PluginDefinitionCompilerTest.class.getClassLoader());
    }
}
