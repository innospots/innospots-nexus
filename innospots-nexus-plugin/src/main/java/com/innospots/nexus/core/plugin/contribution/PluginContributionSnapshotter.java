package com.innospots.nexus.core.plugin.contribution;

import java.util.Map;

/** 将 Contribution 转为不含类、Handler 或 Secret 的安全快照。 */
public interface PluginContributionSnapshotter<T extends PluginContribution> {

    /**
     * 返回该快照器支持的类型。
     *
     * @return 快照器处理的 Contribution 类型标识
     */
    PluginContributionType<T> type();

    /**
     * 生成通用、不可变、可序列化的静态摘要。
     *
     * @param contribution 待摘要的 Contribution
     * @return 不含类引用、Handler 或 Secret 的键值映射
     */
    Map<String, Object> snapshot(T contribution);
}
