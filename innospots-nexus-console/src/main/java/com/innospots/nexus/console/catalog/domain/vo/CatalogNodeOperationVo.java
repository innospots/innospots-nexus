package com.innospots.nexus.console.catalog.domain.vo;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.console.endpoint.grant.EndpointGrantAction;

/**
 * 目录树节点可绑定的 REST 操作（来自 {@link com.innospots.nexus.console.endpoint.grant.EndpointGrantManager}）。
 *
 * @param name   稳定业务名（与 grant {@code name} 一致）
 * @param label  多语言操作描述
 * @param action 动作分类
 * @param path   JAX-RS 路径模板
 */
@Schema(name = "CatalogNodeOperationVo", description = "目录节点关联的 API 操作")
public record CatalogNodeOperationVo(
        @Schema(description = "稳定业务名", required = true)
        String name,
        @Schema(description = "多语言操作描述", required = true)
        I18nObject label,
        @Schema(description = "动作分类", required = true)
        EndpointGrantAction action,
        @Schema(description = "JAX-RS 路径模板", required = true)
        String path
) {

    public CatalogNodeOperationVo {
        name = name == null ? "" : name;
        label = label == null ? new I18nObject() : label;
        path = path == null ? "" : path;
    }
}
