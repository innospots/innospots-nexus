package com.innospots.nexus.core.plugin.runtime;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.capability.CapabilityKey;
import com.innospots.nexus.core.plugin.capability.CapabilityType;
import com.innospots.nexus.core.plugin.capability.Tags;
import com.innospots.nexus.core.plugin.config.ConfigDefinition;
import com.innospots.nexus.core.plugin.contract.CapabilityProvider;
import com.innospots.nexus.core.plugin.contract.CapabilityProviderContext;
import com.innospots.nexus.core.plugin.contract.Plugin;
import com.innospots.nexus.core.plugin.contract.PluginContext;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;
import com.innospots.nexus.core.plugin.discovery.DiscoveredPlugin;
import com.innospots.nexus.core.plugin.discovery.PluginCatalog;
import com.innospots.nexus.core.plugin.lifecycle.PluginState;
import com.innospots.nexus.core.plugin.support.PluginTestLog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 覆盖插件定义、配置、发现、Capability 路由、生命周期转换和边界行为的端到端场景测试，
 * 同时输出结构化诊断日志。
 */
class PluginRuntimeScenariosTest {

    private static final CapabilityType<GreetingProvider> GREETING =
            CapabilityType.of("scenario.greeting", 1, GreetingProvider.class);
    private static final CapabilityType<AuditProvider> AUDIT =
            CapabilityType.of("scenario.audit", 1, AuditProvider.class);

    private final PluginTestLog log = new PluginTestLog(PluginRuntimeScenariosTest.class, "runtime");

    @Test
    void runsCompletePluginLifecycleWithConfigurationAndCapabilityUsage() {
        List<String> lifecycle = new CopyOnWriteArrayList<>();
        ConfigurablePlugin plugin = new ConfigurablePlugin(lifecycle);

        log.section("definition");
        log.dumpDefinition(plugin.definition());

        PluginRuntimeConfig config = new PluginRuntimeConfig(
                Set.of("com.example.scenario-configurable"),
                Set.of(),
                Map.of("plugins.com.example.scenario-configurable.endpoint", "https://api.example.com"),
                Map.of("plugins.com.example.scenario-configurable.token", "secret-token"),
                Map.of(),
                getClass().getClassLoader());
        log.dumpMap("host config", config.hostConfig());
        log.dumpMap("runtime variables", config.runtimeVariables());

        try (DefaultPluginManager manager = DefaultPluginManager.create(
                config,
                PluginCatalog.of(List.of(discovered(plugin))),
                List.of())) {
            manager.start();
            log.dumpRuntime(manager);

            GreetingProvider greeting = manager.capabilities().require(
                    GREETING, Tags.of("scenario", "configurable"));
            String message = greeting.greet("nexus");
            log.info("capability result=%s", message);

            assertThat(message).isEqualTo("hello nexus from https://api.example.com");
            assertThat(manager.plugin("com.example.scenario-configurable")).get()
                    .extracting(info -> info.state())
                    .isEqualTo(PluginState.ACTIVE);
            assertThat(lifecycle).contains(
                    "plugin-initialize",
                    "provider-initialize",
                    "plugin-start");

            manager.stop("com.example.scenario-configurable");
            log.dumpRuntime(manager);
            assertThat(manager.plugin("com.example.scenario-configurable")).get()
                    .extracting(info -> info.state())
                    .isEqualTo(PluginState.STOPPED);
            assertThat(lifecycle).contains("provider-destroy", "plugin-stop", "resource-close");
        }
    }

    @Test
    void routesSameCapabilityAcrossDifferentTaggedPlugins() {
        List<String> appLifecycle = new CopyOnWriteArrayList<>();
        List<String> robotLifecycle = new CopyOnWriteArrayList<>();
        GreetingAppPlugin appPlugin = new GreetingAppPlugin(appLifecycle);
        GreetingRobotPlugin robotPlugin = new GreetingRobotPlugin(robotLifecycle);

        PluginRuntimeConfig config = new PluginRuntimeConfig(
                Set.of(),
                Set.of(),
                Map.of(),
                Map.of(),
                Map.of(GREETING.key(), Tags.of("mode", "robot")),
                getClass().getClassLoader());

        try (DefaultPluginManager manager = DefaultPluginManager.create(
                config,
                PluginCatalog.of(List.of(discovered(appPlugin), discovered(robotPlugin))),
                List.of())) {
            manager.start();
            log.dumpRuntime(manager);

            GreetingProvider app = manager.capabilities().require(GREETING, Tags.of("mode", "app"));
            GreetingProvider robot = manager.capabilities().require(GREETING, Tags.empty());
            GreetingProvider defaulted = manager.capabilities().require(GREETING, Tags.of("mode", "robot"));

            log.info("app greeting=%s", app.greet("app"));
            log.info("robot greeting=%s", robot.greet("robot"));
            log.info("default greeting=%s", defaulted.greet("default"));

            assertThat(app.greet("app")).isEqualTo("app:hello app");
            assertThat(robot.greet("robot")).isEqualTo("robot:hello robot");
            assertThat(defaulted).isSameAs(robot);
            assertThat(manager.capabilities().findAll(GREETING)).hasSize(2);
        }
    }

    @Test
    void startsOptionalDependentWithoutOptionalCapabilityProvider() {
        List<String> lifecycle = new CopyOnWriteArrayList<>();
        OptionalAuditConsumer consumer = new OptionalAuditConsumer(lifecycle);

        try (DefaultPluginManager manager = DefaultPluginManager.create(
                runtimeConfig(Map.of()),
                PluginCatalog.of(List.of(discovered(consumer))),
                List.of())) {
            manager.start();
            log.dumpRuntime(manager);

            assertThat(manager.plugin("com.example.scenario-audit-consumer")).get()
                    .extracting(info -> info.state())
                    .isEqualTo(PluginState.ACTIVE);
            assertThat(manager.capabilities().findAll(AUDIT)).isEmpty();
            assertThat(lifecycle).contains("consumer-start-without-audit");
        }
    }

    @Test
    void skipsDisabledPluginsDuringStartup() {
        List<String> lifecycle = new CopyOnWriteArrayList<>();
        BackgroundPlugin background = new BackgroundPlugin(lifecycle);
        GreetingAppPlugin app = new GreetingAppPlugin(lifecycle);

        PluginRuntimeConfig config = new PluginRuntimeConfig(
                Set.of(),
                Set.of("com.example.scenario-background"),
                Map.of(),
                Map.of(),
                Map.of(),
                getClass().getClassLoader());

        try (DefaultPluginManager manager = DefaultPluginManager.create(
                config,
                PluginCatalog.of(List.of(discovered(background), discovered(app))),
                List.of())) {
            manager.start();
            log.dumpRuntime(manager);

            assertThat(manager.plugin("com.example.scenario-background")).get()
                    .extracting(info -> info.state())
                    .isEqualTo(PluginState.DESCRIBED);
            assertThat(lifecycle).doesNotContain("background-start");
            assertThat(manager.plugin("com.example.scenario-greeting-app")).get()
                    .extracting(info -> info.state())
                    .isEqualTo(PluginState.ACTIVE);
        }
    }

    @Test
    void recreatesProvidersAfterStopAndRestart() {
        List<String> lifecycle = new CopyOnWriteArrayList<>();
        List<Integer> providerInstances = new CopyOnWriteArrayList<>();
        RestartablePlugin plugin = new RestartablePlugin(lifecycle, providerInstances);

        try (DefaultPluginManager manager = DefaultPluginManager.create(
                runtimeConfig(Map.of()),
                PluginCatalog.of(List.of(discovered(plugin))),
                List.of())) {
            manager.start();
            int firstInstance = providerInstances.getFirst();
            log.info("first provider instance=%s", firstInstance);

            manager.stop("com.example.scenario-restartable");
            manager.start("com.example.scenario-restartable");
            int secondInstance = providerInstances.getLast();
            log.info("second provider instance=%s", secondInstance);
            log.dumpRuntime(manager);

            assertThat(firstInstance).isNotEqualTo(secondInstance);
            assertThat(lifecycle)
                    .contains("plugin-start", "provider-destroy", "plugin-stop")
                    .contains("plugin-initialize", "provider-initialize");
        }
    }

    @Test
    void recordsFailureDiagnosticsWithoutPublishingPartialCapabilities() {
        List<String> lifecycle = new CopyOnWriteArrayList<>();
        FailingStartupPlugin plugin = new FailingStartupPlugin(lifecycle);

        try (DefaultPluginManager manager = DefaultPluginManager.create(
                runtimeConfig(Map.of()),
                PluginCatalog.of(List.of(discovered(plugin))),
                List.of())) {
            manager.start();

            log.dumpRuntime(manager);
            log.dumpLifecycle("failure lifecycle", lifecycle);

            assertThat(manager.plugin("com.example.scenario-failing")).get()
                    .extracting(info -> info.state())
                    .isEqualTo(PluginState.FAILED);
            assertThat(manager.plugin("com.example.scenario-failing")).get()
                    .extracting(info -> info.lastError())
                    .isNotNull();
            assertThat(manager.capabilities().findAll(GREETING)).isEmpty();
            assertThat(lifecycle).containsExactly(
                    "plugin-initialize",
                    "provider-initialize",
                    "plugin-start-failure",
                    "provider-destroy",
                    "plugin-stop",
                    "resource-close");
        }
    }

    @Test
    void rejectsCapabilityLookupWhenNoProviderMatches() {
        GreetingAppPlugin app = new GreetingAppPlugin(new CopyOnWriteArrayList<>());

        try (DefaultPluginManager manager = DefaultPluginManager.create(
                runtimeConfig(Map.of()),
                PluginCatalog.of(List.of(discovered(app))),
                List.of())) {
            manager.start();

            assertThatThrownBy(() -> manager.capabilities().require(
                    GREETING, Tags.of("mode", "missing")))
                    .isInstanceOf(NexusException.class)
                    .hasMessageContaining("not found");
        }
    }

    private PluginRuntimeConfig runtimeConfig(Map<CapabilityKey, Tags> routes) {
        return new PluginRuntimeConfig(
                Set.of(),
                Set.of(),
                Map.of(),
                Map.of(),
                routes,
                getClass().getClassLoader());
    }

    private static DiscoveredPlugin discovered(Plugin plugin) {
        return new DiscoveredPlugin(plugin, plugin.definition(), Instant.now());
    }

    private interface GreetingProvider extends CapabilityProvider {

        String greet(String name);
    }

    private interface AuditProvider extends CapabilityProvider {

        void record(String action);
    }

    private static final class ConfigurablePlugin implements Plugin {

        private final List<String> lifecycle;

        private ConfigurablePlugin(List<String> lifecycle) {
            this.lifecycle = lifecycle;
        }

        @Override
        public PluginDefinition definition() {
            return PluginDefinition.builder("com.example.scenario-configurable")
                    .name("Scenario Configurable Plugin")
                    .version("1.0.0")
                    .tags(Tags.of("scenario", "configurable"))
                    .provide(GREETING, () -> new ConfigurableGreetingProvider(lifecycle))
                    .config(ConfigDefinition.builder()
                            .string("endpoint").required().end()
                            .secret("token").required().end()
                            .build())
                    .build();
        }

        @Override
        public void initialize(PluginContext context) {
            lifecycle.add("plugin-initialize");
            context.resources().add(() -> lifecycle.add("resource-close"));
        }

        @Override
        public void start() {
            lifecycle.add("plugin-start");
        }

        @Override
        public void stop() {
            lifecycle.add("plugin-stop");
        }
    }

    private static final class ConfigurableGreetingProvider implements GreetingProvider {

        private final List<String> lifecycle;
        private String endpoint;

        private ConfigurableGreetingProvider(List<String> lifecycle) {
            this.lifecycle = lifecycle;
        }

        @Override
        public void initialize(CapabilityProviderContext context) {
            lifecycle.add("provider-initialize");
            endpoint = context.config().require("endpoint");
            context.config().requireSecret("token").close();
        }

        @Override
        public void destroy() {
            lifecycle.add("provider-destroy");
        }

        @Override
        public String greet(String name) {
            return "hello " + name + " from " + endpoint;
        }
    }

    private abstract static class TaggedGreetingPlugin implements Plugin {

        private final String id;
        private final String mode;
        private final List<String> lifecycle;

        private TaggedGreetingPlugin(String id, String mode, List<String> lifecycle) {
            this.id = id;
            this.mode = mode;
            this.lifecycle = lifecycle;
        }

        @Override
        public PluginDefinition definition() {
            return PluginDefinition.builder(id)
                    .name("Scenario Greeting " + mode)
                    .version("1.0.0")
                    .tags(Tags.of("scenario", "greeting").and("mode", mode))
                    .provide(GREETING, () -> new TaggedGreetingProvider(mode))
                    .build();
        }

        @Override
        public void start() {
            lifecycle.add(id + "-start");
        }
    }

    private static final class GreetingAppPlugin extends TaggedGreetingPlugin {

        private GreetingAppPlugin(List<String> lifecycle) {
            super("com.example.scenario-greeting-app", "app", lifecycle);
        }
    }

    private static final class GreetingRobotPlugin extends TaggedGreetingPlugin {

        private GreetingRobotPlugin(List<String> lifecycle) {
            super("com.example.scenario-greeting-robot", "robot", lifecycle);
        }
    }

    private record TaggedGreetingProvider(String mode) implements GreetingProvider {

        @Override
        public String greet(String name) {
            return mode + ":hello " + name;
        }
    }

    private static final class OptionalAuditConsumer implements Plugin {

        private final List<String> lifecycle;

        private OptionalAuditConsumer(List<String> lifecycle) {
            this.lifecycle = lifecycle;
        }

        @Override
        public PluginDefinition definition() {
            return PluginDefinition.builder("com.example.scenario-audit-consumer")
                    .name("Scenario Audit Consumer")
                    .version("1.0.0")
                    .tags(Tags.of("scenario", "consumer"))
                    .require(AUDIT, false)
                    .build();
        }

        @Override
        public void start() {
            lifecycle.add("consumer-start-without-audit");
        }
    }

    private static final class BackgroundPlugin implements Plugin {

        private final List<String> lifecycle;

        private BackgroundPlugin(List<String> lifecycle) {
            this.lifecycle = lifecycle;
        }

        @Override
        public PluginDefinition definition() {
            return PluginDefinition.builder("com.example.scenario-background")
                    .name("Scenario Background Plugin")
                    .version("1.0.0")
                    .tags(Tags.of("scenario", "background"))
                    .build();
        }

        @Override
        public void start() {
            lifecycle.add("background-start");
        }
    }

    private static final class RestartablePlugin implements Plugin {

        private final List<String> lifecycle;
        private final List<Integer> providerInstances;

        private RestartablePlugin(List<String> lifecycle, List<Integer> providerInstances) {
            this.lifecycle = lifecycle;
            this.providerInstances = providerInstances;
        }

        @Override
        public PluginDefinition definition() {
            return PluginDefinition.builder("com.example.scenario-restartable")
                    .name("Scenario Restartable Plugin")
                    .version("1.0.0")
                    .tags(Tags.of("scenario", "restartable"))
                    .provide(GREETING, () -> new RestartableGreetingProvider(lifecycle, providerInstances))
                    .build();
        }

        @Override
        public void initialize(PluginContext context) {
            lifecycle.add("plugin-initialize");
        }

        @Override
        public void start() {
            lifecycle.add("plugin-start");
        }

        @Override
        public void stop() {
            lifecycle.add("plugin-stop");
        }
    }

    private static final class RestartableGreetingProvider implements GreetingProvider {

        private final List<String> lifecycle;
        private final List<Integer> providerInstances;

        private RestartableGreetingProvider(List<String> lifecycle, List<Integer> providerInstances) {
            this.lifecycle = lifecycle;
            this.providerInstances = providerInstances;
            providerInstances.add(System.identityHashCode(this));
        }

        @Override
        public void initialize(CapabilityProviderContext context) {
            lifecycle.add("provider-initialize");
        }

        @Override
        public void destroy() {
            lifecycle.add("provider-destroy");
        }

        @Override
        public String greet(String name) {
            return "restartable:" + providerInstances.getLast() + ":" + name;
        }
    }

    private static final class FailingStartupPlugin implements Plugin {

        private final List<String> lifecycle;

        private FailingStartupPlugin(List<String> lifecycle) {
            this.lifecycle = lifecycle;
        }

        @Override
        public PluginDefinition definition() {
            return PluginDefinition.builder("com.example.scenario-failing")
                    .name("Scenario Failing Plugin")
                    .version("1.0.0")
                    .tags(Tags.of("scenario", "failing"))
                    .provide(GREETING, () -> new FailingGreetingProvider(lifecycle))
                    .build();
        }

        @Override
        public void initialize(PluginContext context) {
            lifecycle.add("plugin-initialize");
            context.resources().add(() -> lifecycle.add("resource-close"));
        }

        @Override
        public void start() {
            lifecycle.add("plugin-start-failure");
            throw new RuntimeException("expected startup failure");
        }

        @Override
        public void stop() {
            lifecycle.add("plugin-stop");
        }
    }

    private static final class FailingGreetingProvider implements GreetingProvider {

        private final List<String> lifecycle;

        private FailingGreetingProvider(List<String> lifecycle) {
            this.lifecycle = lifecycle;
        }

        @Override
        public void initialize(CapabilityProviderContext context) {
            lifecycle.add("provider-initialize");
        }

        @Override
        public void destroy() {
            lifecycle.add("provider-destroy");
        }

        @Override
        public String greet(String name) {
            return "never";
        }
    }
}
