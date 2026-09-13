package com.innospots.nexus.core.plugin.contribution.console;

import java.util.List;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.contribution.PluginContribution;
import com.innospots.nexus.core.plugin.contribution.PluginContributionType;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/** console@1 管理模块、页面和菜单的静态资源贡献。 */
public record ConsolePluginContribution(List<ConsoleModuleDeclaration> modules)
        implements PluginContribution {

    /** Console Contribution 的稳定类型。 */
    public static final PluginContributionType<ConsolePluginContribution> TYPE =
            new PluginContributionType<>("console", 1);

    /** 防御性复制模块列表。 */
    public ConsolePluginContribution {
        modules = modules == null ? List.of() : List.copyOf(modules);
        if (modules.isEmpty()) {
            throw NexusException.build(PluginStatusCode.RESOURCE_CONFLICT,
                    "console contribution must declare at least one module");
        }
    }

    /** 返回 console@1 类型。 */
    @Override
    public PluginContributionType<ConsolePluginContribution> type() {
        return TYPE;
    }
}
