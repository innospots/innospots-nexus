package com.innospots.nexus.console.navigation.endpoint;

import java.util.List;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.thread.TLC;
import com.innospots.nexus.console.menu.domain.vo.NavigationMenuVo;
import com.innospots.nexus.console.navigation.service.NavigationMenuAssembler;
import com.innospots.nexus.console.permission.authorization.AuthorizationSubjectResolver;

/** 当前用户可见的侧边栏导航接口。 */
@Path("/console/navigation/menus")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public final class NavigationMenuEndpoint {

    private final NavigationMenuAssembler assembler;
    private final AuthorizationSubjectResolver subjectResolver;

    /** 创建导航菜单接口。 */
    public NavigationMenuEndpoint(
            NavigationMenuAssembler assembler,
            AuthorizationSubjectResolver subjectResolver
    ) {
        if (assembler == null || subjectResolver == null) {
            throw NexusException.build(NexusStatusCode.CONFIG_ERROR,
                    "assembler and subjectResolver are required");
        }
        this.assembler = assembler;
        this.subjectResolver = subjectResolver;
    }

    /**
     * 返回当前用户可见的 MENU 树。
     *
     * @return 导航树；未登录时返回空列表
     */
    @GET
    public R<List<NavigationMenuVo>> listNavigationMenus() {
        String workspaceId = TLC.workspaceId();
        if (workspaceId == null || workspaceId.isBlank()) {
            throw NexusException.build(NexusStatusCode.CONFIG_ERROR, "Workspace context is required");
        }
        return R.ok(subjectResolver.resolve()
                .map(subject -> assembler.navigationMenus(workspaceId, subject))
                .orElseGet(List::of));
    }
}
