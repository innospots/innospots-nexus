package com.innospots.nexus.platform.permission.service;

import java.util.List;
import java.util.Objects;

import lombok.RequiredArgsConstructor;

import com.innospots.nexus.base.thread.SessionContext;
import com.innospots.nexus.console.jaxrs.web.ConsoleWebSecuritySettings;
import com.innospots.nexus.console.permission.authorization.AuthorizationSubjectResolver;
import com.innospots.nexus.console.permission.pageaccess.PageAccessVerifier;
import com.innospots.nexus.console.permission.pageaccess.PageAccessVerifyRequest;
import com.innospots.nexus.console.permission.pageaccess.PageAccessVerifyResult;
import com.innospots.nexus.console.scope.ConsoleSessionContext;
import com.innospots.nexus.console.scope.ConsoleSessionScope;
import com.innospots.nexus.platform.permission.domain.request.ActionAccessItem;
import com.innospots.nexus.platform.permission.domain.request.ActionAccessRequest;
import com.innospots.nexus.platform.permission.domain.vo.ActionAccessVo;

/**
 * 运营平台页面访问校验（开放 HTTP；有会话时按 catalog 授权判定）。
 */
@RequiredArgsConstructor
public class PlatformPageAccessService {

    private final PageAccessVerifier pageAccessVerifier;
    private final AuthorizationSubjectResolver subjectResolver;
    private final PlatformActionAccessService actionAccessService;
    private final ConsoleWebSecuritySettings securitySettings;

    /** 校验页面和页面所需请求，使用当前会话而不是调用方指定的主体。 */
    public PageAccessVerifyResult verify(PageAccessVerifyRequest request) {
        if (!securitySettings.isEnabled()) {
            return PageAccessVerifyResult.allowIgnoringGrantGroup();
        }
        if (request == null || (!"authenticated".equals(request.accessMode())
                && !"grant".equals(request.accessMode()))) {
            return PageAccessVerifyResult.deny("Invalid access mode");
        }
        if ("grant".equals(request.accessMode())
                && (request.pageKey() == null || request.pageKey().isBlank()
                || !actionAccessService.hasPageGrant(request.domainKey(), request.moduleKey(),
                        request.pageKey(), request.grantGroup()))) {
            return PageAccessVerifyResult.deny("Invalid page grant configuration");
        }
        PageAccessVerifyResult page = subjectResolver.resolve()
                .map(subject -> {
                    if ("authenticated".equals(request.accessMode())) {
                        return PageAccessVerifyResult.allow();
                    }
                    if (ConsoleSessionContext.scope() == ConsoleSessionScope.PLATFORM) {
                        return pageAccessVerifier.verify(request, null, subject);
                    }
                    String workspaceId = SessionContext.workspaceId();
                    if (workspaceId == null || workspaceId.isBlank()) {
                        return PageAccessVerifyResult.deny("Workspace is required");
                    }
                    return pageAccessVerifier.verify(request, workspaceId, subject);
                })
                .orElseGet(() -> PageAccessVerifyResult.deny("Authentication is required"));
        if (!page.allowed()) {
            return page;
        }
        List<ActionAccessItem> requests = request.requests() == null ? List.of() : request.requests().stream()
                .filter(Objects::nonNull)
                .map(item -> new ActionAccessItem(item.method(), item.url()))
                .toList();
        ActionAccessVo actions = actionAccessService.verify(
                new ActionAccessRequest(request.domainKey(), request.moduleKey(), requests));
        return new PageAccessVerifyResult(true, null, actions.allowed(), actions.grantGroups(), false);
    }
}
