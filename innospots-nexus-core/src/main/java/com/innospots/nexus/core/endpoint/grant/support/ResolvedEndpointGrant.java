package com.innospots.nexus.core.endpoint.grant.support;

import com.innospots.nexus.core.endpoint.grant.EndpointGrantAction;
import com.innospots.nexus.core.endpoint.grant.I18nLiteral;

/**
 * 合并模块与方法注解后的有效 endpoint 策略。
 *
 * @param moduleKey         entry 模块键；未声明模块时为空字符串
 * @param domainKey         领域键（来自 {@link com.innospots.nexus.core.endpoint.grant.EndpointModule#domain()}）
 * @param grantGroup        浏览/操作分组（来自 {@link com.innospots.nexus.core.endpoint.grant.EndpointGrant#group()}，缺省按 action 解析为 view/operate）
 * @param action            动作类型
 * @param name              稳定业务名
 * @param label             审计文案
 * @param recordArgs        是否记录参数
 * @param auditLog          是否写审计
 * @param ignorePermission  是否跳过权限
 * @param ignoreAuth        是否跳过登录
 */
public record ResolvedEndpointGrant(
        String moduleKey,
        String domainKey,
        String grantGroup,
        EndpointGrantAction action,
        String name,
        I18nLiteral label,
        boolean recordArgs,
        boolean auditLog,
        boolean ignorePermission,
        boolean ignoreAuth
) {

    /** 浏览/导出类 endpoint 在 {@link #group()} 缺省时的分组键。 */
    public static final String VIEW_GRANT_GROUP = "view";

    /** 可变更类 endpoint 在 {@link #group()} 缺省时的分组键。 */
    public static final String OPERATE_GRANT_GROUP = "operate";

    public ResolvedEndpointGrant {
        moduleKey = moduleKey == null ? "" : moduleKey;
        domainKey = domainKey == null ? "" : domainKey;
        name = name == null ? "" : name;
        grantGroup = resolveGrantGroup(grantGroup, action);
    }

    /**
     * {@link com.innospots.nexus.core.endpoint.grant.EndpointGrant#group()} 为空时：
     * READ/EXPORT → {@link #VIEW_GRANT_GROUP}，其余可变更动作 → {@link #OPERATE_GRANT_GROUP}。
     */
    public static String resolveGrantGroup(String rawGroup, EndpointGrantAction action) {
        if (rawGroup != null && !rawGroup.isBlank()) {
            return rawGroup.trim();
        }
        if (action == EndpointGrantAction.READ || action == EndpointGrantAction.EXPORT) {
            return VIEW_GRANT_GROUP;
        }
        return OPERATE_GRANT_GROUP;
    }

    /**
     * 写入授权表时使用的 {@code grantName} 段（READ/EXPORT 按分组，其余按 {@link #name()}）。
     */
    public String permissionGrantName() {
        if (action == EndpointGrantAction.READ || action == EndpointGrantAction.EXPORT) {
            return grantGroup;
        }
        return name;
    }
}
