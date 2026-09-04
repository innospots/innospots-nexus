package com.innospots.nexus.console.catalog.endpoint;

import java.util.List;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.catalog.domain.vo.CatalogNodeVo;
import com.innospots.nexus.console.catalog.service.ConsoleCatalogService;
import com.innospots.nexus.core.plugin.contribution.console.catalog.domain.model.CatalogSyncResult;
import com.innospots.nexus.core.plugin.contribution.console.catalog.service.ConsoleCatalogSyncService;
import com.innospots.nexus.console.permission.domain.vo.PermissionResourceSyncVo;

/** 权限设置页目录树与显式同步接口。 */
@Path("/console/catalog")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public final class ConsoleCatalogEndpoint {

    private final ConsoleCatalogService catalogService;
    private final ConsoleCatalogSyncService syncService;

    /** 创建目录接口。 */
    public ConsoleCatalogEndpoint(
            ConsoleCatalogService catalogService,
            ConsoleCatalogSyncService syncService
    ) {
        this.catalogService = catalogService;
        this.syncService = syncService;
    }

    /** 返回已启用的插件目录资源树。 */
    @GET
    @Path("/tree")
    public R<List<CatalogNodeVo>> tree() {
        return R.ok(catalogService.tree());
    }

    /** 将 ACTIVE 插件贡献同步到权限目录。 */
    @POST
    @Path("/sync")
    public R<PermissionResourceSyncVo> sync() {
        CatalogSyncResult result = syncService.sync();
        return R.ok(new PermissionResourceSyncVo(
                result.createdResources(), result.updatedResources(), result.disabledResources()));
    }
}
