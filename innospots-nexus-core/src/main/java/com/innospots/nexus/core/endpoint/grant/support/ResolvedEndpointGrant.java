package com.innospots.nexus.core.endpoint.grant.support;

import com.innospots.nexus.core.endpoint.grant.EndpointGrantAction;
import com.innospots.nexus.core.endpoint.grant.I18nLiteral;

/**
 * 合并模块与方法注解后的有效 endpoint 策略。
 *
 * @param moduleKey         entry 模块键；未声明模块时为空字符串
 * @param domainKey         领域键（来自 {@link com.innospots.nexus.core.endpoint.grant.EndpointModule#domain()}）
 * @param grantGroup        浏览/操作分组（来自 {@link com.innospots.nexus.core.endpoint.grant.EndpointGrant#group()}，缺省 {@code default}）
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

    public static final String DEFAULT_GRANT_GROUP = "default";

    public ResolvedEndpointGrant {
        moduleKey = moduleKey == null ? "" : moduleKey;
        domainKey = domainKey == null ? "" : domainKey;
        grantGroup = grantGroup == null || grantGroup.isBlank() ? DEFAULT_GRANT_GROUP : grantGroup;
        name = name == null ? "" : name;
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
