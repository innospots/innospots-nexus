package com.innospots.nexus.base.resources;

import java.io.IOException;
import java.util.Optional;

/**
 * 二进制资源持久化与读取的抽象接口。
 * 实现可存储于本地（文件系统）、远程（S3、OSS）或数据库。
 * 每个存储实现拥有唯一的 {@link #storeMode()} 标识符。
 *
 * @author Smars
 * @date 2026/09/13
 * @see MetaResource
 * @see FileResource
 */
public interface ResourceStore {

    /**
     * 保存文件资源并关联模块上下文。
     *
     * @param resource  文件资源
     * @param module    模块名称
     * @param moduleKey 模块键
     * @return 持久化后的元数据
     */
    MetaResource save(FileResource resource, String module, String moduleKey);

    /**
     * 按资源 ID 读取二进制内容。
     *
     * @param resourceId 资源 ID
     * @return 二进制内容，不存在时返回空
     */
    Optional<byte[]> read(String resourceId);

    /**
     * 按资源 ID 删除资源。
     *
     * @param resourceId 资源 ID
     * @return 删除成功时返回 {@code true}
     */
    boolean delete(String resourceId);

    /**
     * 判断资源是否存在。
     *
     * @param resourceId 资源 ID
     * @return 存在时返回 {@code true}
     */
    boolean exists(String resourceId);

    /**
     * 返回存储模式标识符。
     *
     * @return 存储模式
     */
    String storeMode();
}
