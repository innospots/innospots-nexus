package com.innospots.nexus.console.openapi.domain.vo;

/**
 * classpath 上的单个 OpenAPI 模块规范。
 *
 * @param specId   规范标识（{@code <specId>.yaml} 的文件名主体）
 * @param fileName 打包文件名
 */
public record OpenApiSpecItemVo(String specId, String fileName) {
}
