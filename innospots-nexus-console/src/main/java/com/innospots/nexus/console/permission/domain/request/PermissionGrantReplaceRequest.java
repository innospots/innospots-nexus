package com.innospots.nexus.console.permission.domain.request;

import java.util.List;

/** 一个角色或组织单元最终应拥有的完整资源授权集合。 */
public record PermissionGrantReplaceRequest(
        /** 前端提交的完整授权集合；空集合表示清空该主体的授权。 */
        List<PermissionGrantItemRequest> grants
) {

    /** 将空请求规范化为空集合，并复制集合避免调用方后续修改请求内容。 */
    public PermissionGrantReplaceRequest {
        grants = grants == null ? List.of() : List.copyOf(grants);
    }
}
