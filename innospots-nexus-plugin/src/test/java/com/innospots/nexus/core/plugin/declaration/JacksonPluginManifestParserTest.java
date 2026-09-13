package com.innospots.nexus.core.plugin.declaration;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** 验证 YAML 解析器的严格结构和安全限制。 */
class JacksonPluginManifestParserTest {

    private final JacksonPluginManifestParser parser = new JacksonPluginManifestParser();

    @Test
    void parsesMinimalManifestAndMapsYamlClassField() {
        PluginManifest manifest = parser.parse("""
                apiVersion: nexus.plugin/v1
                kind: Plugin
                metadata:
                  pluginId: com.example.message
                  version: 1.0.0
                spec:
                  apiVersion: 1
                  displayName:
                    zh-CN: 消息插件
                  capabilities:
                    - type: message.sender
                      majorVersion: 1
                      providerId: wecom
                      bind:
                        kind: java
                        class: com.example.MessageSender
                """);

        assertThat(manifest.metadata().pluginId()).isEqualTo("com.example.message");
        assertThat(manifest.spec().capabilities().getFirst().bind().className())
                .isEqualTo("com.example.MessageSender");
    }

    @Test
    void rejectsUnknownFieldsDuplicateKeysAndAliases() {
        assertThatThrownBy(() -> parser.parse("""
                apiVersion: nexus.plugin/v1
                kind: Plugin
                unexpected: true
                metadata: {pluginId: com.example.message, version: 1}
                spec: {apiVersion: 1, displayName: {en: message}, capabilities: []}
                """))
                .isInstanceOf(NexusException.class);
        assertThatThrownBy(() -> parser.parse("apiVersion: nexus.plugin/v1\napiVersion: nexus.plugin/v1\n"))
                .isInstanceOf(NexusException.class);
        assertThatThrownBy(() -> parser.parse("value: &base x\n"))
                .isInstanceOf(NexusException.class);
    }
}
