package com.innospots.nexus.platform.permission.domain.vo;

import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.innospots.nexus.platform.permission.domain.request.ActionAccessRequest;

/**
 * 页面操作的授权结果与页面模块的授权分组。
 *
 * @author Smars
 * @date 2026/11/04
 * @param allowed 方法和路径组成的键对应的判定结果
 * @param grantGroups 页面模块中当前主体获准的授权组
 * @see ActionAccessRequest
 */
@Schema(name = "ActionAccessVo", description = "页面操作权限检查结果")
public record ActionAccessVo(
        @Schema(description = "按 method:url 索引的权限结果") Map<String, Boolean> allowed,
        @Schema(description = "页面模块当前可用的授权分组") List<String> grantGroups
) {

    public ActionAccessVo {
        allowed = allowed == null ? Map.of() : Map.copyOf(allowed);
        grantGroups = grantGroups == null ? List.of() : List.copyOf(grantGroups);
    }
}
