package com.innospots.nexus.core.plugin.contribution;

import java.util.List;

/**
 * 从 {@link PluginContribution} 定义中汇总扩展规模，供发现与生命周期日志使用。
 *
 * @author Smars
 * @date 2026/10/15
 */
public final class PluginContributionInventories {

    private PluginContributionInventories() {
    }

    /**
     * 返回单个贡献的模块数；未实现 {@link PluginContributionInventory} 时返回 0。
     *
     * @param contribution 插件贡献声明
     * @return 模块数
     */
    public static int moduleCount(PluginContribution contribution) {
        if (contribution instanceof PluginContributionInventory inventory) {
            return inventory.moduleCount();
        }
        return 0;
    }

    /**
     * 返回单个贡献的组件数；未实现 {@link PluginContributionInventory} 时返回 0。
     *
     * @param contribution 插件贡献声明
     * @return 组件数
     */
    public static int componentCount(PluginContribution contribution) {
        if (contribution instanceof PluginContributionInventory inventory) {
            return inventory.componentCount();
        }
        return 0;
    }

    /**
     * 汇总插件定义中全部贡献的模块与组件数量。
     *
     * @param contributions 贡献列表
     * @return 汇总结果
     */
    public static Summary summarize(List<PluginContribution> contributions) {
        if (contributions == null || contributions.isEmpty()) {
            return new Summary(0, 0, 0);
        }
        int modules = 0;
        int components = 0;
        for (PluginContribution contribution : contributions) {
            modules += moduleCount(contribution);
            components += componentCount(contribution);
        }
        return new Summary(contributions.size(), modules, components);
    }

    /**
     * 插件全部 Contribution 的规模汇总。
     *
     * @param contributionCount 贡献条目数
     * @param moduleCount 扩展模块总数
     * @param componentCount 扩展组件总数
     */
    public record Summary(int contributionCount, int moduleCount, int componentCount) {
    }
}
