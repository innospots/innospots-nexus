package com.innospots.nexus.console.endpoint.grant;

import java.util.List;

/**
 * 按模块聚合的 grant 目录，供前端按模块读取。
 *
 * @param moduleKey         模块键
 * @param readOnly          只读类接口（{@link EndpointGrantAction#READ}、{@link EndpointGrantAction#EXPORT}）
 * @param operable          可变更/执行类接口
 * @param ignoreAuth        跳过登录鉴权的接口
 * @param ignorePermission  跳过权限校验的接口
 */
public record EndpointModuleGrantCatalogVo(
        String moduleKey,
        List<EndpointGrantDescriptorVo> readOnly,
        List<EndpointGrantDescriptorVo> operable,
        List<EndpointGrantDescriptorVo> ignoreAuth,
        List<EndpointGrantDescriptorVo> ignorePermission
) {

    public EndpointModuleGrantCatalogVo {
        moduleKey = moduleKey == null ? "" : moduleKey;
        readOnly = readOnly == null ? List.of() : List.copyOf(readOnly);
        operable = operable == null ? List.of() : List.copyOf(operable);
        ignoreAuth = ignoreAuth == null ? List.of() : List.copyOf(ignoreAuth);
        ignorePermission = ignorePermission == null ? List.of() : List.copyOf(ignorePermission);
    }
}
