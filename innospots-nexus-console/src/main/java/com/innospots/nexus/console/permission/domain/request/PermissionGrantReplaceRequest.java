package com.innospots.nexus.console.permission.domain.request;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * 一个角色或组织单元最终应拥有的完整资源授权集合。
 *
 * @author Smars
 * @date 2026/09/13
 */
@Schema(name = "PermissionGrantReplaceRequest", description = "权限授权全量替换请求")
public record PermissionGrantReplaceRequest(
        @Schema(description = "前端提交的完整授权集合；空集合表示清空该主体的授权", required = true)
        List<PermissionGrantItemRequest> grants
) {

    /**
     * 将空请求规范化为空集合，并复制集合避免调用方后续修改请求内容。
     */
    public PermissionGrantReplaceRequest {
        grants = grants == null ? List.of() : List.copyOf(grants);
    }
}
