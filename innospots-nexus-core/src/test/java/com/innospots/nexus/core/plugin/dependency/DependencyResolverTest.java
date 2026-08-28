package com.innospots.nexus.core.plugin.dependency;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.capability.CapabilityRegistry;
import com.innospots.nexus.core.plugin.capability.CapabilityType;
import com.innospots.nexus.core.plugin.capability.Tags;
import com.innospots.nexus.core.plugin.contract.CapabilityProvider;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DependencyResolverTest {

    private static final CapabilityType<Provider> PROVIDER =
            CapabilityType.of("dependency.fixture", 1, Provider.class);

    @Test
    void rejectsNullPublicInputsWithNexusExceptions() {
        CapabilityRegistry registry = new CapabilityRegistry(Map.of());
        PluginDefinition definition = PluginDefinition.builder("dependency-fixture")
                .name("Dependency Fixture")
                .version("1.0.0")
                .tags(Tags.of("fixture", "dependency"))
                .require(PROVIDER, true)
                .build();

        assertThatThrownBy(() -> new DependencyResolver(null, registry))
                .isInstanceOf(NexusException.class);
        assertThatThrownBy(() -> new DependencyResolver(List.of(definition), null))
                .isInstanceOf(NexusException.class);

        DependencyResolver resolver = new DependencyResolver(List.of(definition), registry);
        assertThatThrownBy(() -> resolver.resolve(null)).isInstanceOf(NexusException.class);
        assertThatThrownBy(() -> resolver.canStart(null)).isInstanceOf(NexusException.class);
    }

    private interface Provider extends CapabilityProvider {
    }
}
