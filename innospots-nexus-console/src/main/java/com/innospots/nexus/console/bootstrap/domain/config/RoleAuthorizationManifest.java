package com.innospots.nexus.console.bootstrap.domain.config;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * 角色授权清单（classpath {@code console/bootstrap/roles.yaml}）。
 *
 * <p>该文件用于在管理端未启用时完成角色与资源授权初始化：服务启动读取 classpath
 * 下所有同名文件并合并，然后把差异写入 {@code nx_role} / {@code nx_permission_grant}。
 * 由本文件声明的角色与授权记录标记为只读，不允许在管理端删除、变更或停用。</p>
 *
 * @param roles 角色列表
 */
@Schema(name = "RoleAuthorizationManifest", description = "角色授权清单")
public record RoleAuthorizationManifest(List<RoleDefinition> roles) {

    /**
     * 角色定义。
     *
     * @param roleCode    角色编码，同一安全域内唯一且稳定，用于跨版本识别同一角色
     * @param roleName    角色名称（展示用）
     * @param nameEn      角色英文名称；为空时回退 {@code roleName}
     * @param description 说明
     * @param ownerType   作用域：{@code PLATFORM} / {@code TENANT} / {@code WORKSPACE}
     * @param administrator 是否管理员角色（拥有全部权限的快捷标记）
     * @param sortOrder   排序值
     * @param grants      该角色持有的资源授权，按 {@code domain + module} 聚合
     */
    @Schema(name = "RoleDefinition", description = "角色定义")
    public record RoleDefinition(
            String roleCode,
            String roleName,
            String nameEn,
            String description,
            String ownerType,
            Boolean administrator,
            Integer sortOrder,
            List<ModuleGrant> grants
    ) {

        /**
         * 是否管理员角色，缺省为 false。
         *
         * @return 是否管理员
         */
        public boolean admin() {
            return Boolean.TRUE.equals(administrator);
        }

        /**
         * 排序值，缺省为 1。
         *
         * @return 排序值
         */
        public int order() {
            return sortOrder == null ? 1 : sortOrder;
        }
    }

    /**
     * 单个模块上的授权，按 {@code domain + module} 聚合多个授权分组。
     *
     * <p>落库时展开为多个授权键 {@code domainKey.moduleKey:grantGroup}，
     * 例如 {@code domain=system, module=settings, grantGroups=[view, update]}
     * 会生成 {@code system.settings:view} 与 {@code system.settings:update}。</p>
     *
     * @param domain      领域键
     * @param module      模块键
     * @param grantGroups 授权分组列表（通常为 {@code view} / {@code operate} / {@code create} 等）
     */
    @Schema(name = "ModuleGrant", description = "角色在单个模块上的授权")
    public record ModuleGrant(String domain, String module, List<String> grantGroups) {

        /**
         * 规范化为去空白、去重、保序的分组列表。
         *
         * @return 规范化后的分组列表；空输入返回空列表
         */
        public List<String> normalizedGroups() {
            if (grantGroups == null || grantGroups.isEmpty()) {
                return List.of();
            }
            return grantGroups.stream()
                    .filter(group -> group != null && !group.isBlank())
                    .map(String::trim)
                    .distinct()
                    .toList();
        }
    }
}
