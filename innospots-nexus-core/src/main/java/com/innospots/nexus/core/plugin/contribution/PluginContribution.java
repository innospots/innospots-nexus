package com.innospots.nexus.core.plugin.contribution;

/**
 * 插件向宿主提交的一类不可变静态扩展声明。
 *
 * <p>实现类在定义阶段构造，不包含运行时服务或可变状态。
 */
public interface PluginContribution {

    /**
     * 返回贡献类型及其主版本。
     *
     * @return 类型标识
     */
    PluginContributionType<? extends PluginContribution> type();
}
