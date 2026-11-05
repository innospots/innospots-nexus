package com.innospots.nexus.platform.permission.domain.request;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * 当前用户的页面操作批量校验请求。
 *
 * @author Smars
 * @date 2026/11/04
 * @param domain 页面所属领域键
 * @param module 页面所属模块键
 * @param requests 待校验操作，最多 256 项
 * @see ActionAccessItem
 */
@Schema(name = "ActionAccessRequest", description = "页面操作权限批量校验请求")
public record ActionAccessRequest(
        @Schema(description = "页面领域键") String domain,
        @Schema(description = "页面模块键") String module,
        @Schema(description = "待检查请求列表") List<ActionAccessItem> requests
) {

    public ActionAccessRequest {
        requests = requests == null ? List.of() : Collections.unmodifiableList(new ArrayList<>(requests));
    }
}
