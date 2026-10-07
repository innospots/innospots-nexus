package com.innospots.nexus.core.plugin.resource;

/**
 * 一个资源释放器的幂等注册句柄。
 * @author Smars
 * @date 2026/09/13
 */
public interface ResourceRegistration extends AutoCloseable {

    /** 最多执行资源释放器一次。 */
    @Override
    void close();
}
