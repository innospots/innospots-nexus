package com.innospots.nexus.platform.permission.domain.request;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * 待校验的 HTTP 方法与页面作者态路径。
 *
 * @author Smars
 * @date 2026/11/04
 * @param method HTTP 方法
 * @param url 相对 API 路径，可含独立路径段表达式
 * @see ActionAccessRequest
 */
@Schema(name = "ActionAccessItem", description = "待校验的页面操作")
public record ActionAccessItem(
        @Schema(description = "HTTP 方法", required = true, examples = {"POST"}) String method,
        @Schema(description = "相对 API 路径", required = true, examples = {"/api/d/platform/roles"}) String url
) {
}
