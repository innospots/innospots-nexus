package com.innospots.nexus.core.plugin.runtime;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.capability.CapabilityKey;
import com.innospots.nexus.core.plugin.capability.Tags;
import com.innospots.nexus.core.plugin.support.PluginTestLog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PluginRuntimeConfigTest {

    private final PluginTestLog log = new PluginTestLog(PluginRuntimeConfigTest.class, "runtime-config");

    @Test
    void copiesCollectionsAndResolvesClassLoaderFallback() {
        Map<String, String> hostConfig = new HashMap<>(Map.of("plugins.sample.endpoint", "value"));
        PluginRuntimeConfig config = new PluginRuntimeConfig(
                Set.of("sample"),
                Set.of(),
                hostConfig,
                Map.of(),
                Map.of(),
                null);
        hostConfig.clear();

        ClassLoader fallback = getClass().getClassLoader();
        log.info("resolved class loader=%s", config.resolvedClassLoader(fallback).getName());
        log.dumpMap("host config", config.hostConfig());

        assertThat(config.hostConfig()).containsEntry("plugins.sample.endpoint", "value");
        assertThat(config.resolvedClassLoader(fallback)).isSameAs(fallback);
        assertThatThrownBy(() -> config.requiredPluginIds().add("other"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void rejectsOverlappingRequiredAndDisabledPlugins() {
        assertThatThrownBy(() -> new PluginRuntimeConfig(
                Set.of("sample"),
                Set.of("sample"),
                Map.of(),
                Map.of(),
                Map.of(),
                null))
                .isInstanceOf(NexusException.class)
                .hasMessageContaining("overlap");
    }

    @Test
    void rejectsBlankPluginIdsAndNullRouteEntries() {
        Set<String> blankIds = new HashSet<>();
        blankIds.add(" ");
        assertThatThrownBy(() -> new PluginRuntimeConfig(
                blankIds, Set.of(), Map.of(), Map.of(), Map.of(), null))
                .isInstanceOf(NexusException.class);

        Map<CapabilityKey, Tags> routes = new HashMap<>();
        routes.put(new CapabilityKey("message.push", 1), null);
        assertThatThrownBy(() -> new PluginRuntimeConfig(
                Set.of(), Set.of(), Map.of(), Map.of(), routes, null))
                .isInstanceOf(NexusException.class);
    }
}
