package com.innospots.nexus.core.plugin.support;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.innospots.nexus.core.plugin.config.PluginConfig;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;
import com.innospots.nexus.core.plugin.lifecycle.PluginRuntimeInfo;
import com.innospots.nexus.core.plugin.runtime.PluginManager;

/**
 * Test-facing logger that prints structured plugin diagnostics to both System.Logger and stdout
 * so Maven Surefire output remains readable without an SLF4J binding.
 */
public final class PluginTestLog {

    private final System.Logger logger;
    private final String scenario;

    public PluginTestLog(Class<?> owner, String scenario) {
        this.logger = System.getLogger(owner.getName());
        this.scenario = scenario;
    }

    public void section(String title) {
        String line = "==== [" + scenario + "] " + title + " ====";
        logger.log(System.Logger.Level.INFO, line);
        System.out.println(line);
    }

    public void info(String message) {
        String line = "[" + scenario + "] " + message;
        logger.log(System.Logger.Level.INFO, line);
        System.out.println(line);
    }

    public void info(String message, Object... args) {
        info(String.format(message, args));
    }

    public void dumpRuntime(PluginManager manager) {
        section("runtime snapshot");
        for (PluginRuntimeInfo info : manager.plugins()) {
            info("plugin id=%s name=%s version=%s state=%s phase=%s tags=%s provided=%s requirements=%s deps=%s error=%s",
                    info.id(),
                    info.name(),
                    info.version(),
                    info.state(),
                    info.phase(),
                    info.tags(),
                    info.providedCapabilities(),
                    info.requirements(),
                    summarizeDependencies(info.dependencies()),
                    info.lastError());
        }
    }

    public void dumpMap(String title, Map<?, ?> values) {
        section(title);
        if (values == null || values.isEmpty()) {
            info("(empty)");
            return;
        }
        values.forEach((key, value) -> info("%s = %s", key, value));
    }

    public void dumpCollection(String title, Collection<?> values) {
        section(title);
        if (values == null || values.isEmpty()) {
            info("(empty)");
            return;
        }
        values.forEach(value -> info(String.valueOf(value)));
    }

    public void dumpDefinition(PluginDefinition definition) {
        section("plugin definition");
        info("id=%s name=%s version=%s apiVersion=%s tags=%s",
                definition.id(),
                definition.name(),
                definition.version(),
                definition.apiVersion(),
                definition.tags());
        info("capabilities=%s", definition.capabilities());
        info("requirements=%s", definition.requirements());
        info("config items=%s", definition.config().items());
    }

    public void dumpConfig(PluginConfig config) {
        section("resolved plugin config");
        info("%s", config);
    }

    public void dumpLifecycle(String title, List<String> calls) {
        section(title);
        for (int index = 0; index < calls.size(); index++) {
            info("%02d. %s", index + 1, calls.get(index));
        }
    }

    private static String summarizeDependencies(
            Map<?, ? extends com.innospots.nexus.core.plugin.dependency.DependencyResolution> dependencies
    ) {
        if (dependencies == null || dependencies.isEmpty()) {
            return "{}";
        }
        StringBuilder builder = new StringBuilder("{");
        boolean first = true;
        for (var entry : dependencies.entrySet()) {
            if (!first) {
                builder.append(", ");
            }
            first = false;
            var resolution = entry.getValue();
            builder.append(entry.getKey())
                    .append("(required=")
                    .append(resolution.required())
                    .append(", declared=")
                    .append(resolution.declared())
                    .append(", available=")
                    .append(resolution.available())
                    .append(", providers=")
                    .append(resolution.providerPluginIds())
                    .append(')');
        }
        return builder.append('}').toString();
    }
}
