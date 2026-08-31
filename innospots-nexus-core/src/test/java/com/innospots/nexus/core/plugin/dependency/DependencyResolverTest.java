package com.innospots.nexus.core.plugin.dependency;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.capability.CapabilityKey;
import com.innospots.nexus.core.plugin.capability.CapabilityRegistry;
import com.innospots.nexus.core.plugin.capability.CapabilityType;
import com.innospots.nexus.core.plugin.capability.Tags;
import com.innospots.nexus.core.plugin.contract.CapabilityProvider;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;
import com.innospots.nexus.core.plugin.support.PluginTestLog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DependencyResolverTest {

    private static final CapabilityType<Provider> PROVIDER =
            CapabilityType.of("dependency.fixture", 1, Provider.class);

    private final PluginTestLog log = new PluginTestLog(DependencyResolverTest.class, "dependency");

    @Test
    void reportsDeclaredButUnavailableRequiredDependencies() {
        PluginDefinition consumer = PluginDefinition.builder("dependency-consumer")
                .name("Dependency Consumer")
                .version("1.0.0")
                .tags(Tags.of("fixture", "dependency"))
                .require(PROVIDER, true)
                .build();
        PluginDefinition producer = PluginDefinition.builder("dependency-producer")
                .name("Dependency Producer")
                .version("1.0.0")
                .tags(Tags.of("fixture", "dependency"))
                .provide(PROVIDER, ProviderImpl::new)
                .build();
        CapabilityRegistry registry = new CapabilityRegistry(Map.of());
        DependencyResolver resolver = new DependencyResolver(List.of(consumer, producer), registry);

        Map<CapabilityKey, DependencyResolution> before = resolver.resolve(consumer);
        log.dumpMap("before provider is active", before);
        assertThat(resolver.canStart(before)).isFalse();

        registry.registerAll(List.of(new com.innospots.nexus.core.plugin.capability.CapabilityRegistration<>(
                PROVIDER, new ProviderImpl(), "dependency-producer", Tags.of("fixture", "dependency"))));

        Map<CapabilityKey, DependencyResolution> after = resolver.resolve(consumer);
        log.dumpMap("after provider is active", after);
        assertThat(resolver.canStart(after)).isTrue();
    }

    @Test
    void treatsOptionalMissingDependenciesAsStartable() {
        PluginDefinition consumer = PluginDefinition.builder("optional-consumer")
                .name("Optional Consumer")
                .version("1.0.0")
                .tags(Tags.of("fixture", "dependency"))
                .require(PROVIDER, false)
                .build();
        DependencyResolver resolver = new DependencyResolver(List.of(consumer), new CapabilityRegistry(Map.of()));

        Map<CapabilityKey, DependencyResolution> resolutions = resolver.resolve(consumer);
        log.dumpMap("optional dependency diagnostics", resolutions);

        assertThat(resolver.canStart(resolutions)).isTrue();
        assertThat(resolutions.get(PROVIDER.key()).required()).isFalse();
        assertThat(resolutions.get(PROVIDER.key()).declared()).isFalse();
    }

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

    private static final class ProviderImpl implements Provider {
    }
}
