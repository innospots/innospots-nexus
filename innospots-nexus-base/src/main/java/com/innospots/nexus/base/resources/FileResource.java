package com.innospots.nexus.base.resources;

import java.io.InputStream;

/**
 * 带内容流与元数据标志的文件资源。
 * 当 {@code saveMeta} 为 true 时，资源存储会同时持久化元数据与二进制内容。
 *
 * @author Smars
 * @date 2026/09/13
 * @param name         资源显示名称
 * @param fileName     文件名
 * @param contentType  MIME 类型
 * @param inputStream  内容输入流
 * @param saveMeta     是否同时保存元数据
 * @see ResourceStore
 * @see MetaResource
 */
public record FileResource(
        String name,
        String fileName,
        String contentType,
        InputStream inputStream,
        boolean saveMeta
) {
}
