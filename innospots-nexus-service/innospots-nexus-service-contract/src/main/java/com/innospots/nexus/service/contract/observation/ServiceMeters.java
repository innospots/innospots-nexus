package com.innospots.nexus.service.contract.observation;

import java.time.Duration;
import java.util.Map;

/**
 * 面向业务的计量器。名称必须注册；高基数标签将被拒绝。
 *
 * @author Smars
 * @date 2026/09/13
 * @see MetricsProvider
 */
public interface ServiceMeters {

    /**
     * 递增已注册计数器。
     *
     * @param name 已注册计量器名称
     */
    void increment(String name);

    /**
     * 使用允许的标签递增已注册计数器。
     *
     * @param name 已注册计量器名称
     * @param tags 允许的标签映射
     */
    void increment(String name, Map<String, String> tags);

    /**
     * 向已注册计时器记录时长。
     *
     * @param name     已注册计量器名称
     * @param duration 时长
     */
    void record(String name, Duration duration);
}
