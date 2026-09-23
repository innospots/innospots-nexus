package com.innospots.nexus.console.permission.endpoint;

import java.util.List;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.base.thread.SessionContext;
import com.innospots.nexus.console.permission.authorization.AuthorizationSubjectResolver;
import com.innospots.nexus.console.permission.domain.vo.PermissionResourceVo;
import com.innospots.nexus.console.permission.service.PermissionVisibilityService;
import com.innospots.nexus.core.openapi.NexusAuthenticatedApi;

/**
 * 当前用户可见权限资源的 REST 资源。
 */
@Path("/console/me/permissions")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Permission", description = "授权与可见资源")
@NexusAuthenticatedApi
@RequiredArgsConstructor
public class CurrentAuthorizationEndpoint {

    private final AuthorizationSubjectResolver subjectResolver;
    private final PermissionVisibilityService visibilityService;

    @GET
    @Operation(operationId = "permissionVisibleList", summary = "当前用户可见权限资源")
    public R<List<PermissionResourceVo>> listResources() {
        String workspaceId = SessionContext.requireWorkspaceId();
        return subjectResolver.resolve()
                .map(subject -> visibilityService.visible(workspaceId, subject).stream()
                        .map(PermissionResourceVo::from)
                        .toList())
                .map(R::ok)
                .orElseGet(() -> R.ok(List.of()));
    }
}
