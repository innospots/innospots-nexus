package com.innospots.nexus.console.navigation.endpoint;

import java.util.List;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.thread.SessionContext;
import com.innospots.nexus.console.menu.domain.vo.NavigationMenuVo;
import com.innospots.nexus.console.navigation.service.NavigationMenuAssembler;
import com.innospots.nexus.console.permission.authorization.AuthorizationSubjectResolver;
import com.innospots.nexus.core.openapi.NexusAuthenticatedApi;

/**
 * 当前用户可见的侧边栏导航接口。
 */
@Path("/console/navigation/menus")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Navigation", description = "运行时导航")
@NexusAuthenticatedApi
public final class NavigationMenuEndpoint {

    private final NavigationMenuAssembler assembler;
    private final AuthorizationSubjectResolver subjectResolver;

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

    @GET
    @Operation(operationId = "navigationMenuList", summary = "当前用户可见导航菜单")
    public R<List<NavigationMenuVo>> listNavigationMenus() {
        String workspaceId = SessionContext.requireWorkspaceId();
        return R.ok(subjectResolver.resolve()
                .map(subject -> assembler.navigationMenus(workspaceId, subject))
                .orElseGet(List::of));
    }
}
