package com.innospots.nexus.console.endpoint.grant.support;

import com.innospots.nexus.console.endpoint.grant.EndpointGrantAction;
import com.innospots.nexus.console.logger.I18nLiteral;

/**
 * 合并模块与方法注解后的有效 endpoint 策略。
 *
 * @param moduleKey         entry 模块键；未声明模块时为空字符串
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
        EndpointGrantAction action,
        String name,
        I18nLiteral label,
        boolean recordArgs,
        boolean auditLog,
        boolean ignorePermission,
        boolean ignoreAuth
) {

    public ResolvedEndpointGrant {
        moduleKey = moduleKey == null ? "" : moduleKey;
        name = name == null ? "" : name;
    }
}
