package com.innospots.nexus.core.plugin.declaration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.capability.CapabilityKey;
import com.innospots.nexus.core.plugin.capability.CapabilityType;
import com.innospots.nexus.core.plugin.capability.Tags;
import com.innospots.nexus.core.plugin.config.ConfigDefinition;
import com.innospots.nexus.core.plugin.config.ConfigItemDefinition;
import com.innospots.nexus.core.plugin.contract.CapabilityProvider;
import com.innospots.nexus.core.plugin.contract.CapabilityProviderFactory;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * Immutable static plugin declaration consumed before any lifecycle method executes.
 *
 * @param id stable plugin identifier
 * @param name display name
 * @param version plugin release version
 * @param apiVersion plugin core protocol major version
 * @param tags provider routing identity shared by every contribution
 * @param capabilities capability factories owned by this plugin
 * @param requirements declared capability dependencies
 * @param config plugin configuration schema
 */
public record PluginDefinition(
        String id,
        String name,
        String version,
        int apiVersion,
        Tags tags,
        List<CapabilityContribution<?>> capabilities,
        List<CapabilityRequirement> requirements,
        ConfigDefinition config
) {

    /** Current plugin contract major version. */
    public static final int CURRENT_API_VERSION = 1;

    private static final Pattern ID_PATTERN = Pattern.compile("[a-z][a-z0-9]*(?:-[a-z0-9]+)*");

    /** Validates and defensively copies the complete declaration. */
    public PluginDefinition {
        if (id == null || !ID_PATTERN.matcher(id).matches()) {
            invalid("plugin id must use lowercase kebab-case: " + id);
        }
        if (name == null || name.isBlank() || version == null || version.isBlank()) {
            invalid("plugin name and version are required: " + id);
        }
        if (apiVersion < 1 || tags == null || tags.isEmpty() || config == null) {
            invalid("plugin apiVersion, tags and config are required: " + id);
        }
        capabilities = capabilities == null ? List.of() : List.copyOf(capabilities);
        requirements = requirements == null ? List.of() : List.copyOf(requirements);
        config = snapshotConfig(config);
        requireUniqueCapabilities(capabilities);
        requireUniqueRequirements(requirements);
    }

    /** Creates a fluent declaration builder. */
    public static Builder builder(String id) {
        return new Builder(id);
    }

    private static void requireUniqueCapabilities(List<CapabilityContribution<?>> capabilities) {
        Set<CapabilityKey> keys = new HashSet<>();
        for (CapabilityContribution<?> contribution : capabilities) {
            if (contribution == null || !keys.add(contribution.type().key())) {
                invalid("duplicate capability in plugin definition");
            }
        }
    }

    private static void requireUniqueRequirements(List<CapabilityRequirement> requirements) {
        Set<CapabilityKey> keys = new HashSet<>();
        for (CapabilityRequirement requirement : requirements) {
            if (requirement == null || !keys.add(requirement.key())) {
                invalid("duplicate requirement in plugin definition");
            }
        }
    }

    private static ConfigDefinition snapshotConfig(ConfigDefinition source) {
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

    /** Fluent mutable accumulator that emits one immutable definition. */
    public static final class Builder {

        private final String id;
        private final List<CapabilityContribution<?>> capabilities = new ArrayList<>();
        private final List<CapabilityRequirement> requirements = new ArrayList<>();
        private String name;
        private String version;
        private int apiVersion = CURRENT_API_VERSION;
        private Tags tags = Tags.empty();
        private ConfigDefinition config = ConfigDefinition.empty();

        private Builder(String id) {
            this.id = id;
        }

        /** Sets the display name. */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /** Sets the release version. */
        public Builder version(String version) {
            this.version = version;
            return this;
        }

        /** Overrides the plugin API major version. */
        public Builder apiVersion(int apiVersion) {
            this.apiVersion = apiVersion;
            return this;
        }

        /** Sets routing tags inherited by every provider. */
        public Builder tags(Tags tags) {
            this.tags = tags;
            return this;
        }

        /** Adds one type-safe provider factory. */
        public <T extends CapabilityProvider> Builder provide(
                CapabilityType<T> type,
                CapabilityProviderFactory<? extends T> factory
        ) {
            capabilities.add(new CapabilityContribution<>(type, factory));
            return this;
        }

        /** Adds a capability requirement using its declared type. */
        public Builder require(CapabilityType<?> type, boolean required) {
            if (type == null) {
                invalid("requirement capability type is required");
            }
            requirements.add(new CapabilityRequirement(type.key(), required));
            return this;
        }

        /** Sets the configuration schema. */
        public Builder config(ConfigDefinition config) {
            this.config = config;
            return this;
        }

        /** Creates an immutable, validated definition. */
        public PluginDefinition build() {
            return new PluginDefinition(
                    id,
                    name,
                    version,
                    apiVersion,
                    tags,
                    capabilities,
                    requirements,
                    config);
        }
    }
}
