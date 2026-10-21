package com.innospots.nexus.core.plugin.lifecycle;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.core.plugin.config.ConfigurationManager;
import com.innospots.nexus.core.plugin.contribution.PluginContributionHandler;
import com.innospots.nexus.core.plugin.contribution.console.ConsoleContributionCatalog;
import com.innospots.nexus.core.plugin.contribution.console.ConsoleContributeNode;
import com.innospots.nexus.core.plugin.contribution.console.ConsolePermissionGroupDeclaration;
import com.innospots.nexus.core.plugin.contribution.console.ConsolePluginContribution;
import com.innospots.nexus.core.plugin.contribution.console.ConsolePluginContributionHandler;
import com.innospots.nexus.core.plugin.contribution.console.ReservedPluginResourceCatalog;
import com.innospots.nexus.core.plugin.contract.Plugin;
import com.innospots.nexus.core.plugin.contract.PluginContext;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;
import com.innospots.nexus.core.plugin.discovery.DiscoveredPlugin;
import com.innospots.nexus.core.plugin.event.DefaultPluginEventBus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ManagedPluginTest {

    @Test
    void startsAndStopsPluginWithConsoleContribution() {
        List<String> calls = new ArrayList<>();
        RecordingPlugin plugin = new RecordingPlugin(calls);
        ManagedPlugin managed = managedPlugin(plugin);

        managed.start();
        assertThat(managed.info().state()).isEqualTo(PluginState.ACTIVE);
        assertThat(calls).contains("initialize", "start");

        managed.stop();
        assertThat(managed.info().state()).isEqualTo(PluginState.STOPPED);
        assertThat(calls).contains("stop");
    }

    @Test
    void rollsBackAfterContributionFailure() {
        FailingPlugin plugin = new FailingPlugin();
        ManagedPlugin managed = managedPlugin(plugin);

        assertThatThrownBy(managed::start).isInstanceOf(NexusException.class);
        assertThat(managed.info().state()).isEqualTo(PluginState.FAILED);
    }

    private static ManagedPlugin managedPlugin(Plugin plugin) {
        PluginDefinition definition = plugin.definition();
        List<PluginContributionHandler<?>> handlers = List.of(
                new ConsolePluginContributionHandler(
                        new ConsoleContributionCatalog(),
                        new ReservedPluginResourceCatalog(List.of())));
        return new ManagedPlugin(
                new DiscoveredPlugin(plugin, definition, Instant.now()),
                () -> new ConfigurationManager(Map.of(), Map.of(), Map.of(), Map.of()).resolve(definition),
                new DefaultPluginEventBus(),
                handlers);
    }

    private static ConsolePluginContribution sampleContribution() {
        ConsoleContributeNode menu = new ConsoleContributeNode(
                null,
                I18nObject.of(Map.of("en", "Menu")),
                I18nObject.of(Map.of()),
                0,
                "sample-page",
                null,
                "default",
                null,
                List.of(),
                List.of(),
                List.of());
        ConsoleContributeNode module = new ConsoleContributeNode(
                "role",
                I18nObject.of(Map.of("en", "Role")),
                I18nObject.of(Map.of()),
                0,
                null,
                null,
                null,
                null,
                List.of(new ConsolePermissionGroupDeclaration(
                        "default", I18nObject.of(Map.of("en", "Views")))),
                List.of(new ConsolePermissionGroupDeclaration(
                        "default", I18nObject.of(Map.of("en", "Ops")))),
                List.of(menu));
        return new ConsolePluginContribution(
                "nexus",
                I18nObject.of(Map.of("en", "Nexus")),
                I18nObject.of(Map.of()),
                0,
                List.of(module));
    }

    private static final class RecordingPlugin implements Plugin {

        private final List<String> calls;

        private RecordingPlugin(List<String> calls) {
            this.calls = calls;
        }

        @Override
        public PluginDefinition definition() {
            return PluginDefinition.builder("com.example.lifecycle-fixture")
                    .name("Lifecycle")
                    .version("1.0.0")
                    .contribute(sampleContribution())
                    .build();
        }

        @Override
        public void initialize(PluginContext context) {
            calls.add("initialize");
        }

        @Override
        public void start() {
            calls.add("start");
        }

        @Override
        public void stop() {
            calls.add("stop");
        }
    }

    private static final class FailingPlugin implements Plugin {

        @Override
        public PluginDefinition definition() {
            return PluginDefinition.builder("com.example.lifecycle-fail")
                    .name("Fail")
                    .version("1.0.0")
                    .contribute(sampleContribution())
                    .build();
        }

        @Override
        public void start() {
            throw new IllegalStateException("boom");
        }
    }
}
