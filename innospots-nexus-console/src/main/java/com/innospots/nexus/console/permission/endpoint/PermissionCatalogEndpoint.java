package com.innospots.nexus.console.permission.endpoint;

import java.util.List;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.permission.domain.vo.PermissionResourceSyncVo;
import com.innospots.nexus.console.permission.domain.vo.PermissionResourceVo;

/** 管理端查询权限目录并显式触发目录同步的接口契约。 */
@Path("/console/permissions/catalog")
@Produces(MediaType.APPLICATION_JSON)
public interface PermissionCatalogEndpoint {

    /**
     * 查询当前项目已持久化的规范化权限资源目录。
     *
     * @return 权限资源目录
     */
    @GET
    R<List<PermissionResourceVo>> listResources();

    /**
     * 将当前已激活扩展及其 UiSpec 声明同步到权限目录。
     * 同步过程同时执行来源校验和资源持久化，不提供额外的 validate 接口。
     *
     * @return 本次创建、更新和禁用的资源数量
     */
    @POST
    @Path("/sync")
    R<PermissionResourceSyncVo> syncResources();
}
