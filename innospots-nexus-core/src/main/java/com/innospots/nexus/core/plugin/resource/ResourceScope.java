package com.innospots.nexus.core.plugin.resource;

/**
 * 每次启动周期的资源所有权边界，按注册逆序释放资源。
 */
public interface ResourceScope extends AutoCloseable {

    /**
     * 注册并返回一个可自动关闭的资源。
     *
     * @param resource 当前启动周期拥有的资源
     * @param <T> 资源类型
     * @return 用于流式调用的原资源
     */
    <T extends AutoCloseable> T manage(T resource);

    /**
     * 为不实现 AutoCloseable 的资源注册释放器。
     *
     * @param disposer 最多执行一次的清理操作
     * @return 可幂等取消的注册句柄
     */
    ResourceRegistration add(Runnable disposer);

    /** 释放所有已注册资源。 */
    @Override
    void close();
}
