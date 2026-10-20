package com.innospots.nexus.console.endpoint.grant;

import com.innospots.nexus.base.i18n.I18nObject;

/**
 * 单条 REST 方法的 grant 元数据（前端展示与过滤器策略共用）。
 *
 * @param moduleKey        entry 模块键
 * @param httpMethod       HTTP 动词（大写）
 * @param path             合并后的 JAX-RS 路径模板（类 {@link jakarta.ws.rs.Path} + 方法路径）
 * @param name             稳定业务名
 * @param label            多语言操作描述
 * @param action           动作分类
 * @param recordArgs       审计是否记录参数
 * @param auditLog         是否写审计
 * @param ignorePermission 是否跳过权限校验
 * @param ignoreAuth       是否跳过登录鉴权
 * @param resourceClass    注册时的 JAX-RS 资源类名
 * @param resourceMethod   资源方法名
 */
public record EndpointGrantDescriptorVo(
        String moduleKey,
        String httpMethod,
        String path,
        String name,
        I18nObject label,
        EndpointGrantAction action,
        boolean recordArgs,
        boolean auditLog,
        boolean ignorePermission,
        boolean ignoreAuth,
        String resourceClass,
        String resourceMethod
) {

    public EndpointGrantDescriptorVo {
        moduleKey = moduleKey == null ? "" : moduleKey;
        httpMethod = httpMethod == null ? "" : httpMethod;
        path = path == null ? "" : path;
        name = name == null ? "" : name;
        label = label == null ? new I18nObject() : label;
        resourceClass = resourceClass == null ? "" : resourceClass;
        resourceMethod = resourceMethod == null ? "" : resourceMethod;
    }
}
