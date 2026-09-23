package com.innospots.nexus.console.catalog.endpoint;

import java.util.List;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.catalog.domain.model.CatalogSyncResult;
import com.innospots.nexus.console.catalog.domain.vo.CatalogNodeVo;
import com.innospots.nexus.console.catalog.service.ConsoleCatalogService;
import com.innospots.nexus.console.catalog.service.ConsoleCatalogSyncService;
import com.innospots.nexus.console.permission.domain.vo.PermissionResourceSyncVo;
import com.innospots.nexus.core.openapi.NexusAuthenticatedApi;

/**
 * 权限设置页目录树与显式同步接口。
 */
@Path("/console/catalog")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Catalog", description = "控制台目录索引")
@NexusAuthenticatedApi
public final class ConsoleCatalogEndpoint {

    private final ConsoleCatalogService catalogService;
    private final ConsoleCatalogSyncService syncService;

    public ConsoleCatalogEndpoint(
            ConsoleCatalogService catalogService,
            ConsoleCatalogSyncService syncService
    ) {
        this.catalogService = catalogService;
        this.syncService = syncService;
    }

    @GET
    @Path("/tree")
    @Operation(operationId = "catalogTree", summary = "目录资源树")
    public R<List<CatalogNodeVo>> tree() {
        return R.ok(catalogService.tree());
    }

    @POST
    @Path("/sync")
    @Operation(operationId = "catalogSync", summary = "同步插件贡献到目录索引")
    public R<PermissionResourceSyncVo> sync() {
        CatalogSyncResult result = syncService.sync();
        return R.ok(new PermissionResourceSyncVo(
                result.createdResources(), result.updatedResources(), result.disabledResources()));
    }
}
