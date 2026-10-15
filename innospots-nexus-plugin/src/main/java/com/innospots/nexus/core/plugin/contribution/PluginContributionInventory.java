package com.innospots.nexus.core.plugin.contribution;

/**
 * 描述一类 {@link PluginContribution} 在定义阶段包含的扩展规模。
 *
 * <p>各 Contribution 类型自行定义「模块」与「组件」的语义；宿主仅用于发现期与启动期日志。</p>
 *
 * @author Smars
 * @date 2026/10/15
 */
public interface PluginContributionInventory {

    /**
     * 返回扩展顶层分区数量（例如 console 的管理模块数）。
     *
     * @return 非负模块数
     */
    int moduleCount();

    /**
     * 返回扩展内组件总数量（例如页面节点与菜单节点之和）。
     *
     * @return 非负组件数
     */
    int componentCount();
}
