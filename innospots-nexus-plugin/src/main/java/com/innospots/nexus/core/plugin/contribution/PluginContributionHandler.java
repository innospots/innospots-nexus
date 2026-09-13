package com.innospots.nexus.core.plugin.contribution;

import java.util.List;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.discovery.PluginCatalog;

/**
 * 负责一种 Contribution 全局校验和事务准备的宿主处理器。
 *
 * @param <T> 处理的贡献类型
 */
public interface PluginContributionHandler<T extends PluginContribution> {

    /**
     * 返回该处理器支持的类型。
     *
     * @return 贡献类型标识
     */
    PluginContributionType<T> type();

    /**
     * 对完整插件目录执行无副作用的全局校验。
     *
     * @param catalog 当前有效插件目录
     * @param entries 该类型的全部贡献条目
     * @throws NexusException 全局冲突或约束违反时
     */
    void validate(PluginCatalog catalog, List<PluginContributionEntry<T>> entries);

    /**
     * 准备当前插件资源但不发布。
     *
     * @param context     当前插件启动上下文
     * @param contribution 待准备的贡献声明
     * @return 可在提交或回滚时关闭的预备资源
     * @throws NexusException 准备失败时
     */
    PreparedPluginContribution prepare(PluginContributionContext context, T contribution);
}
