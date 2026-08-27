package com.innospots.nexus.console.permission.endpoint;

import java.util.List;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.permission.domain.vo.PermissionResourceVo;

/** 查询当前用户可见权限资源的管理接口契约。 */
@Path("/console/me/permissions")
@Produces(MediaType.APPLICATION_JSON)
public interface CurrentAuthorizationEndpoint {

    /**
     * 返回当前用户可见的菜单、页面、按钮和 datasource 资源。
     *
     * @return 已按角色和组织单元授权裁剪后的资源集合
     */
    @GET
    R<List<PermissionResourceVo>> listResources();
}
