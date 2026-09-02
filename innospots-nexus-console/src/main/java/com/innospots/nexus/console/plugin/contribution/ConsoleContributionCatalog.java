package com.innospots.nexus.console.plugin.contribution;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.capability.ProviderRef;
import com.innospots.nexus.core.plugin.contribution.PreparedPluginContribution;
import com.innospots.nexus.core.plugin.lifecycle.PluginAvailability;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/** Console Contribution 的活动资源目录，不保存安装事实或 PluginState。 */
public final class ConsoleContributionCatalog {

    private final Map<String, ActiveEntry> active = new LinkedHashMap<>();

    /** 返回当前已通过共享 availability 门控的活动贡献。 */
    public synchronized List<ActiveConsoleContribution> activeContributions() {
        return active.values().stream()
                .filter(entry -> entry.availability().isActive())
                .map(entry -> new ActiveConsoleContribution(
                        entry.ownerPluginId(), entry.contribution(), entry.generation()))
                .toList();
    }

    /** 返回当前插件的活动贡献；不可用或未提交时返回空。 */
    public synchronized java.util.Optional<ActiveConsoleContribution> activeContribution(String pluginId) {
        ActiveEntry entry = active.get(pluginId);
        if (entry == null || !entry.availability().isActive()) {
            return java.util.Optional.empty();
        }
        return java.util.Optional.of(new ActiveConsoleContribution(
                entry.ownerPluginId(), entry.contribution(), entry.generation()));
    }

    /** 为一个插件创建可回滚的目录提交句柄。 */
    PreparedPluginContribution prepare(
            ProviderRef owner,
            ConsolePluginContribution contribution,
            PluginAvailability availability
    ) {
        return new Prepared(owner, contribution, availability);
    }

    private final class Prepared implements PreparedPluginContribution {

        private final ProviderRef owner;
        private final ConsolePluginContribution contribution;
        private final PluginAvailability availability;
        private boolean staged;
        private boolean committed;
        private boolean closed;

        private Prepared(
                ProviderRef owner,
                ConsolePluginContribution contribution,
                PluginAvailability availability
        ) {
            this.owner = owner;
            this.contribution = contribution;
            this.availability = availability;
        }

        @Override
        public synchronized void stage() {
            if (closed) {
                return;
            }
            staged = true;
        }

        @Override
        public synchronized void commit() {
            if (closed || committed) {
                return;
            }
            if (!staged) {
                throw NexusException.build(PluginStatusCode.RESOURCE_CONFLICT,
                        "console contribution must be staged before commit");
            }
            synchronized (ConsoleContributionCatalog.this) {
                ActiveEntry current = active.get(owner.pluginId());
                if (current != null && !current.contribution().equals(contribution)) {
                    throw NexusException.build(PluginStatusCode.RESOURCE_CONFLICT,
                            "active console contribution already exists for plugin: " + owner.pluginId());
                }
                active.put(owner.pluginId(), new ActiveEntry(
                        owner.pluginId(), contribution, availability.generation() + 1, availability));
            }
            committed = true;
        }

        @Override
        public synchronized void rollback() {
            if (!committed) {
                return;
            }
            synchronized (ConsoleContributionCatalog.this) {
                ActiveEntry current = active.get(owner.pluginId());
                if (current != null && current.contribution().equals(contribution)) {
                    active.remove(owner.pluginId());
                }
            }
            committed = false;
        }

        @Override
        public synchronized void close() {
            if (closed) {
                return;
            }
            rollback();
            closed = true;
            staged = false;
        }
    }

    private record ActiveEntry(
            String ownerPluginId,
            ConsolePluginContribution contribution,
            long generation,
            PluginAvailability availability
    ) {
    }

    /** 一个带 ownerPluginId 和提交代次的活动 Console Contribution。 */
    public record ActiveConsoleContribution(
            String ownerPluginId,
            ConsolePluginContribution contribution,
            long generation
    ) {
    }
}
