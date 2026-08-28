package com.innospots.nexus.core.plugin.config;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.capability.Tags;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConfigurationManagerTest {

    @Test
    void resolvesTypedValuesUsingDocumentedSourcePriority() {
        PluginDefinition definition = definition();
        ConfigurationManager manager = new ConfigurationManager(
                Map.of(
                        "plugins.config-fixture.endpoint", "host-endpoint",
                        "plugins.config-fixture.timeout", "20"),
                Map.of("NEXUS_PLUGIN_CONFIG_FIXTURE_TIMEOUT", "30"),
                Map.of("plugins.config-fixture.timeout", "40"),
                Map.of(
                        "plugins.config-fixture.timeout", "50",
                        "plugins.config-fixture.token", "runtime-secret"));

        PluginConfig config = manager.resolve(definition);

        assertThat(config.require("endpoint")).isEqualTo("host-endpoint");
        assertThat(config.getInt("timeout", 0)).isEqualTo(50);
        assertThat(config.getBoolean("enabled", false)).isTrue();
        assertThat(config.getDuration("lease", Duration.ZERO)).isEqualTo(Duration.ofSeconds(15));
        assertThat(config.requireSecret("token").toString()).isEqualTo("******");
        assertThat(config.toString()).doesNotContain("runtime-secret");
        assertThat(config.toString()).doesNotContain("host-endpoint");

        config.requireSecret("token").close();
        String secretCopy = config.requireSecret("token").use(String::new);
        assertThat(secretCopy).isEqualTo("runtime-secret");
    }

    @Test
    void rejectsUnknownAndMissingPluginConfiguration() {
        ConfigurationManager unknown = new ConfigurationManager(
                Map.of("plugins.config-fixture.unknown", "value"),
                Map.of(),
                Map.of(),
                Map.of("plugins.config-fixture.token", "secret"));

        assertThatThrownBy(() -> unknown.resolve(definition()))
                .isInstanceOf(NexusException.class)
                .hasMessageContaining("unknown");

        ConfigurationManager missing = new ConfigurationManager(
                Map.of(), Map.of(), Map.of(), Map.of());
        assertThatThrownBy(() -> missing.resolve(definition()))
                .isInstanceOf(NexusException.class)
                .hasMessageContaining("endpoint");
    }

    @Test
    void rejectsEnvironmentNameCollisionsAcrossPluginDefinitions() {
        PluginDefinition first = PluginDefinition.builder("foo-bar")
                .name("Foo Bar")
                .version("1.0.0")
                .tags(Tags.of("fixture", "config"))
                .config(ConfigDefinition.builder().string("x").end().build())
                .build();
        PluginDefinition second = PluginDefinition.builder("foo")
                .name("Foo")
                .version("1.0.0")
                .tags(Tags.of("fixture", "config"))
                .config(ConfigDefinition.builder().string("bar.x").end().build())
                .build();

        assertThatThrownBy(() -> ConfigurationManager.validateEnvironmentNames(List.of(first, second)))
                .isInstanceOf(NexusException.class)
                .hasMessageContaining("environment");
    }

    @Test
    void rejectsNullDefinitionsAndEnvironmentNamePartsWithNexusExceptions() {
        ConfigurationManager manager = new ConfigurationManager(Map.of(), Map.of(), Map.of(), Map.of());

        assertThatThrownBy(() -> manager.resolve(null)).isInstanceOf(NexusException.class);
        assertThatThrownBy(() -> ConfigurationManager.environmentName(null, "endpoint"))
                .isInstanceOf(NexusException.class);
        assertThatThrownBy(() -> ConfigurationManager.environmentName("config-fixture", null))
                .isInstanceOf(NexusException.class);
    }

    private static PluginDefinition definition() {
        return PluginDefinition.builder("config-fixture")
                .name("Config Fixture")
                .version("1.0.0")
                .tags(Tags.of("fixture", "config"))
                .config(ConfigDefinition.builder()
                        .string("endpoint").required().end()
                        .integer("timeout").defaultValue("10").end()
                        .bool("enabled").defaultValue("true").end()
                        .duration("lease").defaultValue("PT15S").end()
                        .secret("token").required().end()
                        .build())
                .build();
    }
}
