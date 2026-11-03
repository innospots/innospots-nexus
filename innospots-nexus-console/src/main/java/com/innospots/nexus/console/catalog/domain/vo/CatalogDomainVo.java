package com.innospots.nexus.console.catalog.domain.vo;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.innospots.nexus.base.i18n.I18nObject;

/**
 * 权限目录中的 console@2 领域（贡献根）视图；其下 {@link #modules()} 为 MODULE/MENU 树。
 */
@Schema(name = "CatalogDomainVo", description = "权限目录领域（console 贡献根）")
public record CatalogDomainVo(
        @Schema(description = "来源插件", required = true)
        String ownerPluginId,
        @Schema(description = "领域键", required = true)
        String domainKey,
        @Schema(description = "领域展示名称", required = true)
        I18nObject displayName,
        @Schema(description = "领域入口图标")
        String icon,
        @Schema(description = "领域下 MODULE 根节点", required = true)
        List<CatalogNodeVo> modules
) {

    public CatalogDomainVo {
        modules = modules == null ? List.of() : List.copyOf(modules);
    }
}
