package com.innospots.nexus.console.role.domain.vo;

import java.time.LocalDateTime;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.role.domain.enums.RoleOwnerType;

/**
 * 管理控制台角色视图。
 *
 * @author Smars
 * @date 2026/09/13
 * @param roleId         角色标识符
 * @param roleName       显示名称
 * @param roleCode       归属范围内唯一的稳定编码
 * @param ownerType      归属层级
 * @param ownerId        owner 标识符; empty for PLATFORM
 * @param securityRealm  PLATFORM 或 TENANT
 * @param description    可选 描述
 * @param status         生命周期状态
 * @param sortOrder      显示顺序
 * @param builtIn        角色是否由系统管理
 * @param administrator  角色是否绕过普通资源检查
 * @param memberCount    已分配用户数量
 * @param createdAt      创建时间
 * @param updatedAt      最后更新时间
 */
@Schema(name = "RoleVo", description = "角色视图")
public record RoleVo(
        @Schema(description = "角色标识符", required = true)
        String roleId,
        @Schema(description = "显示名称", required = true)
        String roleName,
        @Schema(description = "归属范围内唯一的稳定编码", required = true)
        String roleCode,
        @Schema(description = "归属层级", required = true)
        RoleOwnerType ownerType,
        @Schema(description = "owner 标识符；PLATFORM 时为空")
        String ownerId,
        @Schema(description = "PLATFORM 或 TENANT", required = true)
        SecurityRealm securityRealm,
        @Schema(description = "可选描述")
        String description,
        @Schema(description = "生命周期状态", required = true)
        BasicStatus status,
        @Schema(description = "显示顺序")
        Integer sortOrder,
        @Schema(description = "角色是否由系统管理", required = true)
        Boolean builtIn,
        @Schema(description = "角色是否绕过普通资源检查", required = true)
        Boolean administrator,
        @Schema(description = "已分配用户数量", required = true)
        long memberCount,
        @Schema(description = "创建时间", required = true)
        LocalDateTime createdAt,
        @Schema(description = "最后更新时间", required = true)
        LocalDateTime updatedAt
) {
}
