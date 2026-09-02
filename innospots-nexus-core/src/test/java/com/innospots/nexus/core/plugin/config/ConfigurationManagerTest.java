package com.innospots.nexus.core.plugin.config;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;
import java.util.List;
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
                        "plugins.com.example.config-fixture.endpoint", "host-endpoint",
                        "plugins.com.example.config-fixture.timeout", "20"),
                Map.of("NEXUS_PLUGIN_CONFIG_FIXTURE_TIMEOUT", "30"),
                Map.of("plugins.com.example.config-fixture.timeout", "40"),
                Map.of(
                        "plugins.com.example.config-fixture.timeout", "50",
                        "plugins.com.example.config-fixture.token", "runtime-secret"));

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
    void resolvesValuesFromDynamicConfigSourcesAtResolveTime() {
        PluginDefinition definition = definition();
        ConfigSource database = new ConfigSource() {
            private int calls;

            @Override
            public String name() {
                return "database";
            }

            @Override
            public Map<String, String> values() {
                calls++;
                return Map.of(
                        "plugins.com.example.config-fixture.endpoint", "db-endpoint-" + calls,
                        "plugins.com.example.config-fixture.token", "db-secret");
            }
        };
        ConfigurationManager manager = new ConfigurationManager(
                Map.of("plugins.com.example.config-fixture.endpoint", "host-endpoint"),
                List.of(database),
                Map.of(),
                Map.of(),
                Map.of());

        PluginConfig first = manager.resolve(definition);
        PluginConfig second = manager.resolve(definition);

        assertThat(first.require("endpoint")).isEqualTo("db-endpoint-1");
        assertThat(second.require("endpoint")).isEqualTo("db-endpoint-2");
        first.requireSecret("token").close();
        second.requireSecret("token").close();
    }

    @Test
    void rejectsUnknownAndMissingPluginConfiguration() {
        ConfigurationManager unknown = new ConfigurationManager(
                Map.of("plugins.com.example.config-fixture.unknown", "value"),
                Map.of(),
                Map.of(),
                Map.of("plugins.com.example.config-fixture.token", "secret"));

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
    void resolvesDecimalUriAndEnumConfigurationValues() {
        PluginDefinition definition = PluginDefinition.builder("com.example.typed-config")
                .name("Typed Config")
                .version("1.0.0")
                .tags(Tags.of("fixture", "config"))
                .config(ConfigDefinition.builder()
                        .decimal("threshold").defaultValue("0.5").end()
                        .uri("endpoint").required().end()
                        .enumeration("mode", "fast", "safe").defaultValue("safe").end()
                        .build())
                .build();

        PluginConfig config = new ConfigurationManager(
                Map.of(
                        "plugins.com.example.typed-config.endpoint", "https://example.com/a/../b",
                        "plugins.com.example.typed-config.threshold", "1.25"),
                Map.of(), Map.of(), Map.of()).resolve(definition);

        assertThat(config.getDecimal("threshold", BigDecimal.ZERO)).isEqualByComparingTo("1.25");
        assertThat(config.getUri("endpoint", URI.create("https://fallback.example")))
                .isEqualTo(URI.create("https://example.com/b"));
        assertThat(config.getEnum("mode", "fast")).isEqualTo("safe");
    }

    @Test
    void rejectsInvalidEnumAndRelativeUriValues() {
        PluginDefinition definition = PluginDefinition.builder("com.example.invalid-typed-config")
                .name("Invalid Typed Config")
                .version("1.0.0")
                .tags(Tags.of("fixture", "config"))
                .config(ConfigDefinition.builder()
                        .uri("endpoint").required().end()
                        .enumeration("mode", "fast", "safe").end()
                        .build())
                .build();

        ConfigurationManager invalidUri = new ConfigurationManager(
                Map.of("plugins.com.example.invalid-typed-config.endpoint", "/relative"),
                Map.of(), Map.of(), Map.of());
        assertThatThrownBy(() -> invalidUri.resolve(definition))
                .isInstanceOf(NexusException.class)
                .hasMessageContaining("absolute");

        ConfigurationManager invalidEnum = new ConfigurationManager(
                Map.of("plugins.com.example.invalid-typed-config.endpoint", "https://example.com"),
                Map.of(), Map.of(),
                Map.of("plugins.com.example.invalid-typed-config.mode", "turbo"));
        assertThatThrownBy(() -> invalidEnum.resolve(definition))
                .isInstanceOf(NexusException.class)
                .hasMessageContaining("allowed");
    }

    @Test
    void rejectsEnvironmentNameCollisionsAcrossPluginDefinitions() {
        PluginDefinition first = PluginDefinition.builder("com.example.foo-bar")
                .name("Foo Bar")
                .version("1.0.0")
                .tags(Tags.of("fixture", "config"))
                .config(ConfigDefinition.builder().string("x").end().build())
                .build();
        PluginDefinition second = PluginDefinition.builder("com.example.foo.bar")
                .name("Foo")
                .version("1.0.0")
                .tags(Tags.of("fixture", "config"))
                .config(ConfigDefinition.builder().string("x").end().build())
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
        assertThatThrownBy(() -> ConfigurationManager.environmentName("com.example.config-fixture", null))
                .isInstanceOf(NexusException.class);
    }

    private static PluginDefinition definition() {
        return PluginDefinition.builder("com.example.config-fixture")
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
