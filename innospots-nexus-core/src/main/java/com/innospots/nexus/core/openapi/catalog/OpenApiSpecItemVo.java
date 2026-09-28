package com.innospots.nexus.core.openapi.catalog;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * classpath 上的单个 OpenAPI 模块规范。
 *
 * @param specId   规范标识（{@code <specId>.yaml} 的文件名主体）
 * @param fileName 打包文件名
 */
@Schema(name = "OpenApiSpecItemVo", description = "OpenAPI 规范目录项")
public record OpenApiSpecItemVo(
        @Schema(description = "规范标识", required = true, examples = {"innospots-nexus-console"})
        String specId,
        @Schema(description = "打包 YAML 文件名", required = true, examples = {"innospots-nexus-console.yaml"})
        String fileName) {
}
